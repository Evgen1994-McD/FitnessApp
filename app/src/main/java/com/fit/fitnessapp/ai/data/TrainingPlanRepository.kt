package com.fit.fitnessapp.ai.data

import com.fit.fitnessapp.ai.domain.TrainingPlanAiService
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.PlannedDayModel
import com.fit.fitnessapp.db.TrainingPlanModel
import com.fit.fitnessapp.db.dao.TrainingPlanDao
import kotlinx.coroutines.flow.Flow

class TrainingPlanRepository(
    private val trainingPlanDao: TrainingPlanDao,
    private val trainingPlanAiService: TrainingPlanAiService
) {
    
    suspend fun generateAndSavePlan(
        goal: String,
        targetZones: List<String>,
        availableDays: List<Int>,
        timePerSession: Int
    ): Long {
        val result = trainingPlanAiService.generateTrainingPlan(
            goal = goal,
            targetZones = targetZones,
            availableDays = availableDays,
            timePerSession = timePerSession
        )
        
        // Сохраняем план
        val planId = trainingPlanDao.insertPlan(result.plan)
        
        // Сохраняем запланированные дни
        val plannedDays = result.plannedDays.map { dayResult ->
            PlannedDayModel(
                planId = planId.toInt(),
                dayNumber = dayResult.dayNumber,
                exerciseIds = dayResult.exerciseIds.joinToString(","),
                restDay = dayResult.restDay,
                targetZone = dayResult.targetZone,
                estimatedTime = dayResult.estimatedTime,
                estimatedCalories = dayResult.estimatedCalories
            )
        }
        
        trainingPlanDao.insertPlannedDays(plannedDays)
        
        return planId
    }
    
    suspend fun getActivePlan(): TrainingPlanModel? {
        return trainingPlanDao.getActivePlan()
    }
    
    suspend fun getPlannedDays(planId: Int): List<PlannedDayModel> {
        return trainingPlanDao.getPlannedDays(planId)
    }
    
    fun getAllPlans(): Flow<List<TrainingPlanModel>> {
        return trainingPlanDao.getAllPlans()
    }
    
    suspend fun activatePlan(planId: Int) {
        trainingPlanDao.deactivateAllPlans()
        trainingPlanDao.activatePlan(planId)
    }
    
    suspend fun deletePlan(planId: Int) {
        trainingPlanDao.deletePlannedDays(planId)
        trainingPlanDao.deletePlan(planId)
    }
    
    suspend fun getExerciseRecommendations(
        targetZone: String,
        excludeIds: List<Int>
    ): List<ExerciseModel> {
        return trainingPlanAiService.getExerciseRecommendations(targetZone, excludeIds)
    }
}
