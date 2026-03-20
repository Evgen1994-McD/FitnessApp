package com.fit.fitnessapp.statistic.ui.models

data class MonthlyCaloriesModel(
    val weekNumber: Int, // Номер недели месяца (1, 2, 3, 4, 5)
    val weekRange: String, // Диапазон дат недели (например, "1-7 мар")
    val calories: Int, // Сумма калорий за неделю
    val weekStart: String, // Начальная дата недели в формате "dd.MM.yyyy"
    val weekEnd: String // Конечная дата недели в формате "dd.MM.yyyy"
)

enum class CalendarPeriod {
    WEEK,
    MONTH
}
