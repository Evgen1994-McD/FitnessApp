package com.example.fitnessapp.ai.domain

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
        
        val zonesString = targetZones.joinToString(",")
        
        val prompt = """
            Составь план тренировок для $goal. 
            Зоны: ${targetZones.joinToString(", ")}. 
            Доступные дни: ${availableDays.joinToString(", ")}. 
            Время на сессию: $timePerSession минут. 
            Используй упражнения из базы данных для домашних тренировок. 
            Определи сложность количеством повторений (начинающий: 8-12, средний: 12-15, продвинутый: 15-20).
            
            ВАЖНО: Упражнение может работать с несколькими зонами одновременно (например: "hands,body").
            При выборе упражнений учитывай, что одно упражнение может охватывать несколько целевых зон.
            
            Ответь в формате JSON:
            {
                "name": "Название плана",
                "description": "Описание плана",
                "days": [
                    {
                        "dayNumber": 1,
                        "targetZone": "hands,body",
                        "exercises": [
                            {
                                "id": 1,
                                "repetitions": 12
                            }
                        ],
                        "restDay": false,
                        "estimatedTime": 30,
                        "estimatedCalories": 150
                    }
                ]
            }
        """.trimIndent()
        
        val response = cactusRepository.generateResponse(prompt)
        
        // Парсим ответ и создаем TrainingPlanResult
        return parseTrainingPlanResponse(response, zonesString)
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
    
    private suspend fun parseTrainingPlanResponse(response: String, zonesJson: String): TrainingPlanResult {
        // Упрощенный парсинг - просто создаем базовый план
        // В реальном приложении здесь будет более сложный парсинг AI ответа
        val plan = TrainingPlanModel(
            name = "AI План тренировок",
            description = "Персонализированный план сгенерированный AI",
            targetZones = zonesJson,
            exercisesPerDay = 5,
            aiGenerated = true,
            createdAt = System.currentTimeMillis(),
            isActive = false
        )
        
        // Заглушка для дней - будет заполнена при реальной генерации
        val plannedDays = listOf<PlannedDayResult>()
        
        return TrainingPlanResult(plan, plannedDays)
    }
    
    private suspend fun parseDayPlanResponse(response: String): DayPlanResult {
        // Упрощенный парсинг
        val exercises = getExerciseRecommendations("hands", emptyList())
        val repetitions = exercises.associateBy({ it.id ?: 0 }) { 12 }
        
        return DayPlanResult(exercises, repetitions)
    }
}
