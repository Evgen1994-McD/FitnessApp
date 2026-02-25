package com.example.fitnessapp.utils

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
    
    fun matchesZones(exerciseZone: String?, targetZone: String): Boolean {
        if (exerciseZone.isNullOrEmpty()) return false
        
        val exerciseZones = exerciseZone.split(",").map { it.trim() }
        return exerciseZones.contains(targetZone.trim())
    }
}
