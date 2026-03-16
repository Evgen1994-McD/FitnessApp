package com.example.fitnessapp.utils

import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.dao.ExerciseDao

object ZoneUtils {
    
    fun getZoneDisplayName(zone: String): String {
        val result = when (zone.lowercase()) {
            "hands" -> "Руки"
            "body" -> "Общие"
            "chest" -> "Грудь"
            "shoulders" -> "Плечи"  // Исправлено на множественное число
            "shoulder" -> "Плечи"   // Добавлено на всякий случай
            "back" -> "Спина"
            "legs" -> "Ноги"
            "abs" -> "Пресс"
            "warm" -> "Разминка"
            "stretch" -> "Растяжка"
            else -> zone
        }
        android.util.Log.d("ZoneUtils", "getZoneDisplayName: '$zone' -> '$result'")
        return result
    }
    
    fun getZonesDisplayNames(zones: String?): String {
        if (zones.isNullOrEmpty()) return ""
        
        val zoneList = zones.split(",")
            .map { it.trim() }
            .map { zone ->
                val translated = getZoneDisplayName(zone)
                android.util.Log.d("ZoneUtils", "Перевод зоны: '$zone' -> '$translated'")
                translated
            }
            .distinct()
            .joinToString(", ")
            
        android.util.Log.d("ZoneUtils", "Результат: '$zones' -> '$zoneList'")
        return zoneList
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
