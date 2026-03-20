package com.fit.fitnessapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fit.fitnessapp.db.PlannedDayModel
import com.fit.fitnessapp.db.TrainingPlanModel
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {
    @Query("SELECT * FROM training_plan_table WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlan(): TrainingPlanModel?
    
    @Query("SELECT * FROM planned_day_table WHERE planId = :planId ORDER BY dayNumber")
    suspend fun getPlannedDays(planId: Int): List<PlannedDayModel>
    
    @Query("SELECT * FROM training_plan_table ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<TrainingPlanModel>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: TrainingPlanModel): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedDay(day: PlannedDayModel): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedDays(days: List<PlannedDayModel>)
    
    @Query("UPDATE training_plan_table SET isActive = 0")
    suspend fun deactivateAllPlans()
    
    @Query("UPDATE training_plan_table SET isActive = 1 WHERE id = :planId")
    suspend fun activatePlan(planId: Int)
    
    @Query("DELETE FROM training_plan_table WHERE id = :planId")
    suspend fun deletePlan(planId: Int)
    
    @Query("DELETE FROM planned_day_table WHERE planId = :planId")
    suspend fun deletePlannedDays(planId: Int)
}
