package com.example.fitnessapp.ai.domain

import android.util.Log
import com.example.fitnessapp.ai.data.CactusAiRepository
import com.example.fitnessapp.ai.utils.ZoneUtils
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.PlannedDayModel
import com.example.fitnessapp.db.TrainingPlanModel
import com.example.fitnessapp.db.dao.ExerciseDao
import kotlinx.coroutines.flow.Flow

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
                val exercises = exerciseDao.getExercisesByZone(zone)
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
        
        // 4. Создаем промпт с реальными упражнениями
        val prompt = """
            Ты - фитнес-тренер. Создай план тренировки для цели: $goal.
            
            $exercisesContext
            
            ТРЕБОВАНИЯ:
            - Используй ТОЛЬКО упражнения из списка выше
            - Используй ТОЛЬКО существующие ID из fitness.db
            - Выбери 3-5 упражнений для каждой тренировки
            - Укажи подходы и повторения (8-15)
            - Ответ в формате JSON с реальными ID упражнений
            
            Пример ответа:
            {
              "name": "План тренировки на $goal",
              "description": "План из предзаполненной БД",
              "days": [{
                "dayNumber": ${availableDays.first()},
                "targetZone": "${targetZones.first()}",
                "exercises": [
                  {"id": 5, "repetitions": 12},
                  {"id": 12, "repetitions": 10}
                ],
                "restDay": false,
                "estimatedTime": $timePerSession,
                "estimatedCalories": 150
              }]
            }
        """.trimIndent()
        
        Log.d("TrainingPlanAiService", "📝 Промпт для AI: ${prompt.take(200)}...")
        
        // 5. Генерируем ответ через текущий Cactus API
        val response = try {
            cactusRepository.generateResponse(prompt)
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка Cactus AI: ${e.message}")
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
    
    suspend fun getExerciseRecommendations(
        targetZone: String,
        excludeIds: List<Int>
    ): List<ExerciseModel> {
        val allExercises = exerciseDao.getAllExercises()
        
        return allExercises
            .filter { exercise -> 
                // Используем утилиту для проверки соответствия зон
                ZoneUtils.matchesZones(exercise.muscleZone, targetZone)
            }
            .filter { excludeIds.contains(it.id).not() }
            .shuffled()
            .take(5)
    }
    
    private suspend fun parseTrainingPlanResponse(response: String, exercisesByZone: Map<String, List<ExerciseModel>>): TrainingPlanResult {
        Log.d("TrainingPlanAiService", "🔍 Парсим ответ AI с реальными ID из fitness.db")
        
        // Получаем все валидные ID из предзаполненной БД
        val allValidIds = exercisesByZone.values.flatten().mapNotNull { it.id }.toSet()
        Log.d("TrainingPlanAiService", "✅ Валидные ID в fitness.db: ${allValidIds.size} - $allValidIds")
        
        try {
            // Извлекаем JSON из ответа AI
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd == 0) {
                Log.w("TrainingPlanAiService", "⚠️ JSON не найден в ответе AI")
                return createFallbackPlanResult("План тренировки", exercisesByZone.keys.joinToString(","), listOf(1), 30, exercisesByZone.values.flatten())
            }
            
            val jsonString = response.substring(jsonStart, jsonEnd)
            Log.d("TrainingPlanAiService", "📝 JSON для парсинга: ${jsonString.take(300)}...")
            
            // Упрощенный парсинг JSON (в реальном приложении здесь будет Gson/Moshi)
            val planName = extractJsonValue(jsonString, "name") ?: "AI План тренировок"
            val planDescription = extractJsonValue(jsonString, "description") ?: "Персонализированный план из fitness.db"
            
            // Создаем базовый план
            val plan = TrainingPlanModel(
                name = planName,
                description = planDescription,
                targetZones = exercisesByZone.keys.joinToString(","),
                exercisesPerDay = 5,
                aiGenerated = true,
                createdAt = System.currentTimeMillis(),
                isActive = false
            )
            
            // Извлекаем упражнения из AI ответа
            val exerciseIds = extractExerciseIds(jsonString, allValidIds)
            Log.d("TrainingPlanAiService", "📊 Извлечено ID упражнений: $exerciseIds")
            
            // Создаем запланированные дни с реальными ID
            val plannedDays = if (exerciseIds.isNotEmpty()) {
                listOf(
                    PlannedDayResult(
                        dayNumber = 1,
                        exerciseIds = exerciseIds,
                        restDay = false,
                        targetZone = exercisesByZone.keys.firstOrNull(),
                        estimatedTime = 30,
                        estimatedCalories = 150.0,
                        repetitions = exerciseIds.associateWith { 12 } // Базовые повторения
                    )
                )
            } else {
                Log.w("TrainingPlanAiService", "⚠️ Не удалось извлечь валидные ID, создаем fallback")
                createFallbackPlanResult(planName, exercisesByZone.keys.joinToString(","), listOf(1), 30, exercisesByZone.values.flatten()).plannedDays
            }
            
            Log.d("TrainingPlanAiService", "✅ План создан: ${plannedDays.size} дней, ${plannedDays.flatMap { it.exerciseIds }.size} упражнений")
            return TrainingPlanResult(plan, plannedDays)
            
        } catch (e: Exception) {
            Log.e("TrainingPlanAiService", "❌ Ошибка парсинга JSON: ${e.message}")
            return createFallbackPlanResult("План тренировки", exercisesByZone.keys.joinToString(","), listOf(1), 30, exercisesByZone.values.flatten())
        }
    }
    
    // Вспомогательные функции для парсинга JSON
    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = """"$key"\s*:\s*"([^"]+)"""".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)
    }
    
    private fun extractExerciseIds(json: String, validIds: Set<Int>): List<Int> {
        val ids = mutableListOf<Int>()
        
        // Ищем все ID в формате {"id": 123, "repetitions": 12}
        val idPattern = """\{"id":\s*(\d+)""".toRegex()
        idPattern.findAll(json).forEach { match ->
            val id = match.groupValues[1].toIntOrNull()
            if (id != null && validIds.contains(id)) {
                ids.add(id)
            }
        }
        
        return ids.distinct().take(5) // Максимум 5 упражнений
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
                    exerciseIds = selectedExercises.mapNotNull { it.id },
                    restDay = false,
                    targetZone = zonesString.split(",").firstOrNull(),
                    estimatedTime = timePerSession,
                    estimatedCalories = 150.0,
                    repetitions = selectedExercises.associateBy({ it.id ?: 0 }) { 12 }
                )
            )
        } else {
            emptyList()
        }
        
        Log.d("TrainingPlanAiService", "✅ Fallback план создан: ${plannedDays.size} дней")
        return TrainingPlanResult(plan, plannedDays)
    }
}
