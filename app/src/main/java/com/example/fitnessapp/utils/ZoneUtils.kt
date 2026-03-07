package com.example.fitnessapp.utils

import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.dao.ExerciseDao

object ZoneUtils {
    
    fun getZoneDisplayName(zone: String): String {
        return when (zone) {
            "hands" -> "руки"
            "body" -> "тело"
            "back" -> "спина"
            "legs" -> "ноги"
            else -> zone
        }
    }
    
    fun getZonesDisplayNames(zones: String?): String {
        if (zones.isNullOrEmpty()) return ""
        
        return zones.split(",")
            .map { it.trim() }
            .map { getZoneDisplayName(it) }
            .distinct()
            .joinToString(", ")
    }
    
    suspend fun getZonesFromExercises(
        exercisesIds: String?,
        exerciseDao: ExerciseDao
    ): List<String> {
        if (exercisesIds.isNullOrEmpty()) return emptyList()
        
        return try {
            // Получаем ID упражнений
            val ids = exercisesIds.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { it.toIntOrNull() }
            
            if (ids.isEmpty()) return emptyList()
            
            // Получаем упражнения из БД
            val exercises = exerciseDao.getExercisesByIds(ids)
            
            // Собираем все зоны из упражнений
            val allZones = exercises
                .mapNotNull { it.muscleZone }
                .flatMap { it.split(",").map { zone -> zone.trim() } }
                .distinct()
            
            // Переводим на русский
            allZones.map { getZoneDisplayName(it) }
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun matchesZones(exerciseZone: String?, targetZone: String): Boolean {
        if (exerciseZone.isNullOrEmpty()) return false
        
        val exerciseZones = exerciseZone.split(",").map { it.trim() }
        return exerciseZones.contains(targetZone.trim())
    }
}
