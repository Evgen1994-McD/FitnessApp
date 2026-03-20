package com.fit.fitnessapp.statistic.ui.models

data class BMIModel(
    val bmiValue: Double,
    val status: BMIStatus,
    val height: Double,
    val weight: Double
)

enum class BMIStatus(val displayName: String, val color: String) {
    UNDERWEIGHT("Недостаточный вес", "#3B82F6"),    // blue
    NORMAL("Норма", "#10B981"),                    // green  
    OVERWEIGHT("Избыточный вес", "#F59E0B"),        // yellow
    OBESE("Ожирение", "#EF4444")                   // red
}

fun calculateBMI(weight: Double, height: Double): BMIModel {
    val heightInMeters = height / 100
    val bmiValue = weight / (heightInMeters * heightInMeters)
    
    val status = when {
        bmiValue < 18.5 -> BMIStatus.UNDERWEIGHT
        bmiValue < 25 -> BMIStatus.NORMAL
        bmiValue < 30 -> BMIStatus.OVERWEIGHT
        else -> BMIStatus.OBESE
    }
    
    return BMIModel(
        bmiValue = bmiValue,
        status = status,
        height = height,
        weight = weight
    )
}
