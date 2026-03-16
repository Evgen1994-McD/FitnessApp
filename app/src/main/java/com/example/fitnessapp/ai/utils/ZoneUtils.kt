package com.example.fitnessapp.ai.utils

import androidx.compose.ui.graphics.Color

/**
 * Утилиты для работы с зонами тренировок
 */
object ZoneUtils {
    
    /**
     * Проверяет, соответствует ли упражнение указанным зонам
     * Поддерживает множественные зоны: "hands,body" или одну: "hands"
     */
    fun matchesZones(exerciseZone: String, targetZones: String): Boolean {
        if (targetZones.isBlank()) return false
        
        val targetZoneList = targetZones.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        return targetZoneList.any { zone ->
            exerciseZone.contains(zone, ignoreCase = true)
        }
    }
    
    /**
     * Форматирует отображение зон для UI
     */
    fun formatZonesDisplay(zones: List<String>): String {
        return zones.joinToString(", ") { zone ->
            when (zone.lowercase()) {
                "hands" -> "Руки"
                "body" -> "Общие"
                "chest" -> "Грудь"
                "shoulders" -> "Плечи"  // Исправлено на множественное число
                "back" -> "Спина"
                "legs" -> "Ноги"
                "abs" -> "Пресс"
                "warm" -> "Разминка"
                "stretch" -> "Растяжка"
                else -> zone
            }
        }
    }
    
    /**
     * Получает цвет для зоны
     */
    fun getZoneColor(zone: String): Color {
        return when (zone.lowercase()) {
            "hands" -> Color(0xFF4CAF50)
            "body" -> Color(0xFF2196F3)
            "chest" -> Color(0xFFFF5722)
            "shoulders" -> Color(0xFF9C27B0)
            "back" -> Color(0xFFFF9800)
            "legs" -> Color(0xFF795548)
            "abs" -> Color(0xFFE91E63)
            "warm" -> Color(0xFF00BCD4)
            "stretch" -> Color(0xFF8BC34A)
            else -> Color(0xFF757575)
        }
    }
    
    /**
     * Проверяет, есть ли пересечение зон
     */
    fun hasZoneOverlap(zones1: String, zones2: String): Boolean {
        val zoneList1 = zones1.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val zoneList2 = zones2.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        return zoneList1.any { zone1 ->
            zoneList2.any { zone2 ->
                zone1.equals(zone2, ignoreCase = true)
            }
        }
    }
}
