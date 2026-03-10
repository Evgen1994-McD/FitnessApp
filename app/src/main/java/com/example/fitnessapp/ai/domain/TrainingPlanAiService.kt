package com.example.fitnessapp.ai.domain

import android.util.Log
import com.example.fitnessapp.ai.data.CactusAiRepository
import com.example.fitnessapp.ai.utils.ZoneUtils
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.PlannedDayModel
import com.example.fitnessapp.db.TrainingPlanModel
import com.example.fitnessapp.db.dao.ExerciseDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeoutOrNull

data class TrainingPlanResult(
    val plan: TrainingPlanModel,
    val plannedDays: List<PlannedDayResult>
)

data class PlannedDayResult(
    val dayNumber: Int,
    val exerciseIds: List<Int>,
    val restDay: Boolean,
    val targetZone: String?,
    val estimatedTime: Int,
    val estimatedCalories: Double,
    val repetitions: Map<Int, Int> // exerciseId -> repetitions
)

data class DayPlanResult(
    val exercises: List<ExerciseModel>,
    val repetitions: Map<Int, Int>
)

class TrainingPlanAiService(
    private val cactusRepository: CactusAiRepository,
    private val exerciseDao: ExerciseDao
) {
    
    /**
     * Ручное скачивание модели
     */
    suspend fun downloadModelManually() {
        return cactusRepository.downloadModelManually()
    }
    
    suspend fun getExerciseRecommendations(
        targetZone: String,
        excludeIds: List<Int> = emptyList()
    ): List<ExerciseModel> {
        Log.d("TrainingPlanAiService", "🔍 Получаем упражнения для зоны: $targetZone, исключая: $excludeIds")
        
        return try {
            val exercises = exerciseDao.getExercisesByZone(4, 174, targetZone)
                .filter { exercise -> 
                    // Используем утилиту для проверки соответствия зон
                    ZoneUtils.matchesZones(exercise.muscleZone, targetZone)
                }
                .filter { excludeIds.contains(it.id).not() }
                .shuffled()
                .take(5)
            
            Log.d("TrainingPlanAiService", "📊 Найдено упражнений: ${exercises.size}")
            exercises.forEach { exercise ->
                Log.d("TrainingPlanAiService", "  - ID:${exercise.id} ${exercise.name}")
            }
            exercises
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка получения упражнений: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun generateTrainingPlan(
        goal: String,              // цель (набор массы, похудение, рельеф)
        targetZones: List<String>, // целевые зоны
        availableDays: List<Int>,  // доступные дни недели
        timePerSession: Int        // время на тренировку
    ): TrainingPlanResult {
        
        Log.d("TrainingPlanAiService", "🚀 Создаю план: goal=$goal, zones=$targetZones")
        
        // 1. Получаем реальные упражнения из предзаполненной БД
        val exercisesByZone = try {
            targetZones.associateWith { zone ->
                Log.d("TrainingPlanAiService", "🔍 Получаем упражнения из fitness.db для зоны: $zone")
                val exercises = exerciseDao.getExercisesByZone(4, 174, zone)
                Log.d("TrainingPlanAiService", "📊 Найдено в fitness.db: ${exercises.size} упражнений")
                exercises.forEach { exercise ->
                    Log.d("TrainingPlanAiService", "  - ID:${exercise.id} ${exercise.name} (${exercise.muscleZone})")
                }
                exercises
            }
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка получения упражнений: ${e.message}")
            return createFallbackPlanResult(goal, targetZones.joinToString(","), availableDays, timePerSession, emptyList())
        }
        
        // 2. Проверяем что есть упражнения из предзаполненной БД
        val totalExercises = exercisesByZone.values.sumOf { it.size }
        if (totalExercises == 0) {
            Log.w("TrainingPlanAiService", "⚠️ В fitness.db не найдено упражнений для зон: $targetZones")
            return createFallbackPlanResult(goal, targetZones.joinToString(","), availableDays, timePerSession, emptyList())
        }
        
        // 3. Создаем контекст с реальными данными из fitness.db
        val exercisesContext = buildString {
            appendLine("УПРАЖНЕНИЯ ИЗ ПРЕДЗАПОЛНЕННОЙ БД:")
            exercisesByZone.forEach { (zone, exercises) ->
                appendLine("\n📍 Зона: $zone (${exercises.size} упражнений)")
                exercises.forEach { exercise ->
                    appendLine("  • ID:${exercise.id} - ${exercise.name}")
                    appendLine("    ${exercise.description}")
                }
            }
        }
        
        // 4. Создаем простой промпт без JSON
        val prompt = """
            Создай план тренировки для начинающих. Зона: руки.
            
            Доступные упражнения:
            ${exercisesByZone["hands"]?.take(5)?.joinToString("\n") { "• ${it.name} (ID: ${it.id})" } ?: ""}
            
            Выбери 3 упражнения и напиши план в таком формате:
            
            План: Тренировка рук для начинающих
            Упражнения:
            1. Сгибание в локте на бицепс - 12 повторений
            2. Вращение рукой (правая) - 10 повторений  
            3. Вращение кистей - 15 повторений
            
            Время: 20 минут
            Калории: 150 ккал
        """.trimIndent()
        
        Log.d("TrainingPlanAiService", "📝 Промпт для AI: ${prompt.take(200)}...")
        
        // 5. Генерируем ответ через текущий Cactus API
        val response = try {
            withTimeoutOrNull(10000) { // 10 секунд таймаут
                cactusRepository.generateResponse(prompt)
            }
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка Cactus AI: ${e.message}")
            null
        }
        
        if (response == null) {
            Log.w("TrainingPlanAiService", "⚠️ AI не ответил, создаем fallback план")
            return createFallbackPlanResult(goal, targetZones.joinToString(","), availableDays, timePerSession, exercisesByZone.values.flatten())
        }
        
        Log.d("TrainingPlanAiService", "🤖 Ответ AI: $response")
        
        // 6. Парсим ответ с реальными ID из fitness.db
        return try {
            parseTrainingPlanResponse(response, exercisesByZone)
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка парсинга: ${e.message}")
            createFallbackPlanResult(goal, targetZones.joinToString(","), availableDays, timePerSession, exercisesByZone.values.flatten())
        }
    }
    
    suspend fun generateDayPlan(
        plan: TrainingPlanModel,
        dayNumber: Int,
        completedExercises: List<ExerciseModel>
    ): DayPlanResult {
        
        val completedIds = completedExercises.map { it.id ?: 0 }
        val targetZones = plan.targetZones.split(",")
        val targetZone = targetZones[(dayNumber - 1) % targetZones.size].trim()
        
        val prompt = """
            Составь тренировку на день $dayNumber. 
            Целевая зона: $targetZone. 
            Исключи упражнения: ${completedIds.joinToString(", ")}. 
            Время: ${plan.exercisesPerDay * 5} минут. 
            Укажи количество повторений для каждого упражнения.
            
            Ответь в формате JSON:
            {
                "exercises": [
                    {
                        "id": 1,
                        "repetitions": 12
                    }
                ]
            }
        """.trimIndent()
        
        val response = cactusRepository.generateResponse(prompt)
        
        return parseDayPlanResponse(response)
    }
    
    private suspend fun parseTrainingPlanResponse(response: String, exercisesByZone: Map<String, List<ExerciseModel>>): TrainingPlanResult {
        Log.d("TrainingPlanAiService", "🔍 Парсим текстовый ответ AI")
        
        // Получаем все валидные ID из предзаполненной БД
        val allValidIds = exercisesByZone.values.flatten().mapNotNull { it.id }.toSet()
        Log.d("TrainingPlanAiService", "✅ Валидные ID в fitness.db: ${allValidIds.size} - $allValidIds")
        
        try {
            // Извлекаем ID упражнений из текстового ответа
            val exerciseIds = extractExerciseIdsFromText(response, allValidIds)
            Log.d("TrainingPlanAiService", "📊 Извлечено ID упражнений: $exerciseIds")
            
            // Создаем план
            val plan = TrainingPlanModel(
                name = "AI План тренировки",
                description = "Персональный план из предзаполненной БД",
                targetZones = exercisesByZone.keys.joinToString(","),
                exercisesPerDay = 5,
                aiGenerated = true,
                createdAt = System.currentTimeMillis(),
                isActive = false
            )
            
            // Создаем запланированные дни с реальными ID
            val plannedDays = if (exerciseIds.isNotEmpty()) {
                listOf(
                    PlannedDayResult(
                        dayNumber = 1,
                        exerciseIds = exerciseIds,
                        restDay = false,
                        targetZone = exercisesByZone.keys.firstOrNull(),
                        estimatedTime = 20,
                        estimatedCalories = 150.0,
                        repetitions = exerciseIds.associateWith { 12 }
                    )
                )
            } else {
                Log.w("TrainingPlanAiService", "⚠️ Не удалось извлечь ID, создаем fallback")
                createFallbackPlanResult("План тренировки", exercisesByZone.keys.joinToString(","), listOf(1), 30, exercisesByZone.values.flatten()).plannedDays
            }
            
            Log.d("TrainingPlanAiService", "✅ План создан: ${plannedDays.size} дней, ${plannedDays.flatMap { it.exerciseIds }.size} упражнений")
            return TrainingPlanResult(plan, plannedDays)
            
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка парсинга текста: ${e.message}")
            return createFallbackPlanResult("План тренировки", exercisesByZone.keys.joinToString(","), listOf(1), 30, exercisesByZone.values.flatten())
        }
    }
    
    // Извлекаем ID из текстового ответа
    private fun extractExerciseIdsFromText(text: String, validIds: Set<Int>): List<Int> {
        val ids = mutableListOf<Int>()
        
        // Ищем ID в тексте (формат: ID: 14 или просто числа)
        val idPattern = """ID:\s*(\d+)|(\d{2,3})""".toRegex()
        idPattern.findAll(text).forEach { match ->
            val id = match.groupValues[1].toIntOrNull() ?: match.groupValues[2].toIntOrNull()
            if (id != null && validIds.contains(id)) {
                ids.add(id)
            }
        }
        
        return ids.distinct().take(3) // Максимум 3 упражнения
    }
    
    private suspend fun parseDayPlanResponse(response: String): DayPlanResult {
        // Упрощенный парсинг
        val exercises = getExerciseRecommendations("hands", emptyList())
        val repetitions = exercises.associateBy({ it.id ?: 0 }) { 12 }
        
        return DayPlanResult(exercises, repetitions)
    }
    
    private suspend fun createFallbackPlanResult(
        goal: String,
        zonesString: String,
        availableDays: List<Int>,
        timePerSession: Int,
        exercises: List<ExerciseModel>
    ): TrainingPlanResult {
        Log.d("TrainingPlanAiService", "🔄 Создаю fallback план из ${exercises.size} упражнений")
        
        val plan = TrainingPlanModel(
            name = "План тренировки на $goal",
            description = "Персональный план для зоны ${zonesString.split(",").firstOrNull() ?: "общая"}",
            targetZones = zonesString,
            exercisesPerDay = 5,
            aiGenerated = false,
            createdAt = System.currentTimeMillis(),
            isActive = false
        )
        
        // Создаем план с реальными упражнениями
        val selectedExercises = if (exercises.isNotEmpty()) {
            exercises.shuffled().take(3)
        } else {
            emptyList()
        }
        
        val plannedDays = if (selectedExercises.isNotEmpty()) {
            listOf(
                PlannedDayResult(
                    dayNumber = availableDays.firstOrNull() ?: 1,
                    exerciseIds = selectedExercises.mapNotNull { exercise -> exercise.id },
                    restDay = false,
                    targetZone = zonesString.split(",").firstOrNull(),
                    estimatedTime = timePerSession,
                    estimatedCalories = 150.0,
                    repetitions = selectedExercises.associateBy({ exercise -> exercise.id ?: 0 }) { 12 }
                )
            )
        } else {
            emptyList()
        }
        
        Log.d("TrainingPlanAiService", "✅ Fallback план создан: ${plannedDays.size} дней")
        return TrainingPlanResult(plan, plannedDays)
    }
}
