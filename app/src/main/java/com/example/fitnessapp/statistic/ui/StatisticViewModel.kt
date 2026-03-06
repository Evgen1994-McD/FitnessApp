package com.example.fitnessapp.statistic.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.statistic.domain.StatisticInteractor
import com.example.fitnessapp.utils.TimeUtils
import com.example.fitnessapp.statistic.ui.models.BMIModel
import com.example.fitnessapp.statistic.ui.models.calculateBMI
import com.example.fitnessapp.statistic.ui.models.WorkoutHistoryModel
import com.example.fitnessapp.statistic.ui.models.WeeklyCaloriesModel
import com.example.fitnessapp.statistic.ui.models.DayCalendarModel
import com.example.fitnessapp.statistic.ui.models.MonthlyCaloriesModel
import com.example.fitnessapp.statistic.ui.models.CalendarPeriod
import com.example.fitnessapp.statistic.ui.models.generateWeekDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

enum class WorkoutFilterType {
    ALL,
    WEEK,
    DAY
}

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val statisticInteractor: StatisticInteractor
) : ViewModel() {
    var year = Calendar.getInstance().get(Calendar.YEAR)
    var month = Calendar.getInstance().get(Calendar.MONTH)

    private val _eventListData = MutableStateFlow<List<EventDay>>(emptyList())
    val eventListData: StateFlow<List<EventDay>> = _eventListData.asStateFlow()
    
    private val _statisticData = MutableStateFlow<StatisticModel?>(null)
    val statisticData: StateFlow<StatisticModel?> = _statisticData.asStateFlow()
    
    private val _weightListData = MutableStateFlow<List<WeightModel>>(emptyList())
    val weightListData: StateFlow<List<WeightModel>> = _weightListData.asStateFlow()
    
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Новые StateFlow для нового дизайна
    private val _bmiData = MutableStateFlow<BMIModel?>(null)
    val bmiData: StateFlow<BMIModel?> = _bmiData.asStateFlow()
    
    private val _workoutHistory = MutableStateFlow<List<WorkoutHistoryModel>>(emptyList())
    val workoutHistory: StateFlow<List<WorkoutHistoryModel>> = _workoutHistory.asStateFlow()
    
    private val _weeklyCalories = MutableStateFlow<List<WeeklyCaloriesModel>>(emptyList())
    val weeklyCalories: StateFlow<List<WeeklyCaloriesModel>> = _weeklyCalories.asStateFlow()
    
    private val _calendarDays = MutableStateFlow<List<DayCalendarModel>>(emptyList())
    val calendarDays: StateFlow<List<DayCalendarModel>> = _calendarDays.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _showTopSheetCalendar = MutableStateFlow(false)
    val showTopSheetCalendar: StateFlow<Boolean> = _showTopSheetCalendar.asStateFlow()
    
    // Инициализируем начало недели на основе текущей даты
    private val _selectedWeekStart = MutableStateFlow(getWeekStartDate(LocalDate.now()))
    val selectedWeekStart: StateFlow<LocalDate> = _selectedWeekStart.asStateFlow()
    
    // Новые StateFlow для переключения периодов
    private val _calendarPeriod = MutableStateFlow(CalendarPeriod.WEEK)
    val calendarPeriod: StateFlow<CalendarPeriod> = _calendarPeriod.asStateFlow()
    
    private val _monthlyCalories = MutableStateFlow<List<MonthlyCaloriesModel>>(emptyList())
    val monthlyCalories: StateFlow<List<MonthlyCaloriesModel>> = _monthlyCalories.asStateFlow()
    
    // StateFlow для фильтрации тренировок
    private val _workoutFilterType = MutableStateFlow(WorkoutFilterType.ALL)
    val workoutFilterType: StateFlow<WorkoutFilterType> = _workoutFilterType.asStateFlow()
    
    // Защита от быстрых двойных кликов
    private var lastFilterChangeTime = 0L
    
    // StateFlow для отфильтрованной истории тренировок
    private val _filteredWorkoutHistory = MutableStateFlow<List<WorkoutHistoryModel>>(emptyList())
    val filteredWorkoutHistory: StateFlow<List<WorkoutHistoryModel>> = _filteredWorkoutHistory.asStateFlow()

    fun getStatisticEvents() = viewModelScope.launch {
        val eventList = ArrayList<EventDay>()
        val statisticList = statisticInteractor.getStatistic()
        statisticList.forEach { statisticModel ->
            eventList.add(
                EventDay(
                    TimeUtils.getCalendarFromDate(statisticModel.date),
                    R.drawable.star
                )
            )
        }
        _eventListData.value = eventList
    }

    fun getStatisticByDate(date: String) = viewModelScope.launch {
        _statisticData.value = statisticInteractor.getStatisticByDate(date)

/*

Получить статистику по дате.
Даже если статистики нет ( null) - мы создадим пустой и отправим (значит пользователь
ещё не занимался)

 */
    }

    fun getWeightByYearAndMonth() = viewModelScope.launch {
_weightListData.value = statisticInteractor.getWeightByYearAndMonth(
            year,
            month
        )
    }

    fun saveWeight(weight: Double) = viewModelScope.launch {
        val cv = Calendar.getInstance()
        val day = cv.get(Calendar.DAY_OF_MONTH)
        val month = cv.get(Calendar.MONTH)
        val year = cv.get(Calendar.YEAR)
        
        // Проверяем, есть ли уже запись веса на сегодня
        val existingWeight = statisticInteractor.getWeightToday(year, month, day)
        
        if (existingWeight != null) {
            // Если запись существует, обновляем её
            statisticInteractor.insertWeight(
                existingWeight.copy(weight = weight)
            )
        } else {
            // Если записи нет, создаём новую
            statisticInteractor.insertWeight(
                WeightModel(
                    null,
                    weight,
                    null,
                    day,
                    month,
                    year = year
                )
            )
        }
        getWeightByYearAndMonth()
    }

    fun updateWeight(weightModel: WeightModel) = viewModelScope.launch {
       statisticInteractor.insertWeight(weightModel)
        getWeightByYearAndMonth()
    }

    fun updateYear(newYear: Int) {
        year = newYear
        _selectedYear.value = newYear
        getWeightByYearAndMonth()
    }

    fun updateMonth(newMonth: Int) {
        month = newMonth
        _selectedMonth.value = newMonth
        getWeightByYearAndMonth()
    }
    
    // Новые методы для нового дизайна
    fun loadNewStatisticsData() = viewModelScope.launch {
        loadBMIData()
        loadWorkoutHistory()
        loadWeeklyCalories()
        loadMonthlyCalories()
        updateCalendarDays()
    }
    
    private fun loadBMIData() = viewModelScope.launch {
        val weightList = statisticInteractor.getYearWeightList()
        val latestWeight = weightList.maxByOrNull { it.weight }
        latestWeight?.let { weight ->
            weight.height?.let { height ->
                _bmiData.value = calculateBMI(weight.weight, height)
            }
        }
    }
    
    private fun loadWorkoutHistory() = viewModelScope.launch {
        val allDays = statisticInteractor.getAllDays()
        Log.d("StatisticViewModel", "DEBUG: Всего дней в базе: ${allDays.size}")
        
        val completedDays = allDays.filter { it.isDone && it.completedDate != null }
        Log.d("StatisticViewModel", "DEBUG: Завершенных дней: ${completedDays.size}")
        
        val sortedDays = completedDays.sortedByDescending { it.completedDate }
        // Загружаем все тренировки для фильтрации
            
        val exerciseList = statisticInteractor.getAllExercise()
        val statisticList = statisticInteractor.getStatistic()
        Log.d("StatisticViewModel", "DEBUG: Статистика: ${statisticList.size} записей")
        
        val workoutHistoryList = sortedDays.map { day ->
            val exercises = getExercisesFromIds(day.exercises, exerciseList)
            // Ищем статистику по dayId вместо индекса
            val dayStatistic = statisticList.find { it.dayId == day.id }
            
            WorkoutHistoryModel(
                id = day.id,
                date = day.completedDate ?: "",
                zone = day.zone,
                difficulty = day.difficulty,
                caloriesBurned = dayStatistic?.kcal ?: 0.0, // Берем калории из статистики
                duration = try { dayStatistic?.workoutTime?.toInt() ?: 0 } catch (e: Exception) { 0 }, // Берем длительность из статистики
                exercises = exercises
            )
        }
        
        Log.d("StatisticViewModel", "DEBUG: История тренировок создана: ${workoutHistoryList.size} элементов")
        _workoutHistory.value = workoutHistoryList
        // Применяем текущий фильтр после загрузки данных
        applyWorkoutFilter()
    }
    
    private fun loadWeeklyCalories() = viewModelScope.launch {
        val statisticList = statisticInteractor.getStatistic()
        val weekStart = _selectedWeekStart.value
        
        val weeklyData = (0..6).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            // Правильно форматируем дату для каждого дня
            val dateString = TimeUtils.formatLocalDate(date)
            // Суммируем все калории за этот день
            val dayStatistics = statisticList.filter { it.date == dateString }
            val totalCalories = dayStatistics.sumOf { it.kcal }
            val dayName = when (dayOffset) {
                0 -> "Пн"
                1 -> "Вт"
                2 -> "Ср"
                3 -> "Чт"
                4 -> "Пт"
                5 -> "Сб"
                6 -> "Вс"
                else -> ""
            }
            
            WeeklyCaloriesModel(
                dayOfWeek = dayName,
                calories = totalCalories.toInt(),
                dayNumber = dayOffset
            )
        }
        _weeklyCalories.value = weeklyData
    }
    
    private fun updateCalendarDays() {
        _calendarDays.value = generateWeekDays(_selectedDate.value)
    }
    
    fun onCalendarDayClick(day: DayCalendarModel) {
        // Находим день недели по имени (Пн, Вт, Ср, Чт, Пт, Сб, Вс)
        val dayOfWeekIndex = when (day.dayName) {
            "Пн" -> 0
            "Вт" -> 1
            "Ср" -> 2
            "Чт" -> 3
            "Пт" -> 4
            "Сб" -> 5
            "Вс" -> 6
            else -> 0
        }
        
        // Вычисляем точную дату выбранного дня
        val selectedDate = _selectedWeekStart.value.plusDays(dayOfWeekIndex.toLong())
        _selectedDate.value = selectedDate
        updateCalendarDays()
        // Загружаем статистику для выбранной даты
        getStatisticByDate(TimeUtils.formatLocalDate(_selectedDate.value))
        // Применяем фильтр тренировок после изменения даты
        applyWorkoutFilter()
    }
    
    fun toggleWorkoutExpanded(workoutId: Int) {
        val currentList = _workoutHistory.value.toMutableList()
        val workoutIndex = currentList.indexOfFirst { it.id == workoutId }
        if (workoutIndex != -1) {
            currentList[workoutIndex] = currentList[workoutIndex].copy(
                isExpanded = !currentList[workoutIndex].isExpanded
            )
            _workoutHistory.value = currentList
            // Применяем фильтр после обновления состояния expanded
            applyWorkoutFilter()
        }
    }
    
    private fun getExercisesFromIds(exerciseIds: String, allExercises: List<ExerciseModel>): List<ExerciseModel> {
        if (exerciseIds.isBlank()) return emptyList()
        
        val ids = exerciseIds.split(",").mapNotNull { it.trim().toIntOrNull() }
        return allExercises.filter { it.id in ids }
    }
    
    fun updateBodyMetrics(height: Double, weight: Double) = viewModelScope.launch {
        val cv = Calendar.getInstance()
        val day = cv.get(Calendar.DAY_OF_MONTH)
        val month = cv.get(Calendar.MONTH)
        val year = cv.get(Calendar.YEAR)
        
        // Проверяем, есть ли уже запись веса на сегодня с ростом
        val existingWeight = statisticInteractor.getWeightToday(year, month, day)
        
        if (existingWeight != null) {
            // Если запись существует, обновляем её с ростом
            statisticInteractor.insertWeight(
                existingWeight.copy(weight = weight, height = height)
            )
        } else {
            // Проверяем, есть ли последняя запись без роста
            val weightList = statisticInteractor.getYearWeightList()
            val latestWeight = weightList.maxByOrNull { it.weight }
            
            if (latestWeight != null && latestWeight.height == null) {
                // Обновляем последнюю запись, добавляя рост
                statisticInteractor.insertWeight(
                    latestWeight.copy(weight = weight, height = height)
                )
            } else {
                // Создаем новую запись
                statisticInteractor.insertWeight(
                    WeightModel(
                        null,
                        weight,
                        height, // Сохраняем рост для будущих расчетов ИМТ
                        day,
                        month,
                        year = year
                    )
                )
            }
        }
        
        // Обновляем BMI данные
        loadBMIData()
        getWeightByYearAndMonth()
    }
    
    // Методы для TopSheet календаря
    fun showTopSheetCalendar() {
        _showTopSheetCalendar.value = true
    }
    
    fun hideTopSheetCalendar() {
        _showTopSheetCalendar.value = false
    }
    
    fun onDateSelected(selectedDate: LocalDate) {
        val weekStart = getWeekStartDate(selectedDate)
        _selectedWeekStart.value = weekStart
        _selectedDate.value = selectedDate
        updateCalendarDays()
        loadWeeklyCalories()
        loadMonthlyCalories()
        hideTopSheetCalendar()
        // Применяем фильтр тренировок после изменения даты
        applyWorkoutFilter()
    }
    
    private fun getWeekStartDate(selectedDate: LocalDate): LocalDate {
        return selectedDate.minusDays(selectedDate.dayOfWeek.value - 1L)
    }
    
    // Методы для фильтрации тренировок
    fun cycleWorkoutFilter() {
        val currentFilter = _workoutFilterType.value
        val newFilter = when (currentFilter) {
            WorkoutFilterType.ALL -> WorkoutFilterType.WEEK
            WorkoutFilterType.WEEK -> WorkoutFilterType.DAY
            WorkoutFilterType.DAY -> WorkoutFilterType.ALL
        }
        _workoutFilterType.value = newFilter
        Log.d("StatisticViewModel", "Filter changed from $currentFilter to $newFilter")
        applyWorkoutFilter()
    }
    
    // Обновленный метод с защитой от двойных кликов
    fun cycleWorkoutFilterSafe() {
        val currentTime = System.currentTimeMillis()
        
        // Защита от быстрых двойных кликов (500ms)
        if (currentTime - lastFilterChangeTime < 500) {
            Log.d("StatisticViewModel", "Ignoring rapid filter click")
            return
        }
        
        val currentFilter = _workoutFilterType.value
        val newFilter = when (currentFilter) {
            WorkoutFilterType.ALL -> WorkoutFilterType.WEEK
            WorkoutFilterType.WEEK -> WorkoutFilterType.DAY
            WorkoutFilterType.DAY -> WorkoutFilterType.ALL
        }
        _workoutFilterType.value = newFilter
        lastFilterChangeTime = currentTime
        Log.d("StatisticViewModel", "Filter changed from $currentFilter to $newFilter")
        applyWorkoutFilter()
    }
    
    private fun applyWorkoutFilter() {
        val allWorkouts = _workoutHistory.value
        val filteredWorkouts = when (_workoutFilterType.value) {
            WorkoutFilterType.ALL -> allWorkouts
            WorkoutFilterType.WEEK -> filterWorkoutsByWeek(allWorkouts)
            WorkoutFilterType.DAY -> filterWorkoutsByDay(allWorkouts)
        }
        _filteredWorkoutHistory.value = filteredWorkouts
    }
    
    private fun filterWorkoutsByWeek(workouts: List<WorkoutHistoryModel>): List<WorkoutHistoryModel> {
        val weekStart = _selectedWeekStart.value
        val weekEnd = weekStart.plusDays(6)
        
        return workouts.filter { workout ->
            try {
                val workoutDate = LocalDate.parse(
                    workout.date, 
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
                workoutDate.isAfter(weekStart.minusDays(1)) && workoutDate.isBefore(weekEnd.plusDays(1))
            } catch (e: Exception) {
                Log.e("StatisticViewModel", "Error parsing workout date: ${workout.date}", e)
                false
            }
        }
    }
    
    private fun filterWorkoutsByDay(workouts: List<WorkoutHistoryModel>): List<WorkoutHistoryModel> {
        val selectedDate = _selectedDate.value
        val selectedDateString = TimeUtils.formatLocalDate(selectedDate)
        
        Log.d("StatisticViewModel", "Filtering by day: looking for date '$selectedDateString' in ${workouts.size} workouts")
        Log.d("StatisticViewModel", "Available workout dates: ${workouts.map { it.date }}")
        
        val filtered = workouts.filter { workout ->
            workout.date == selectedDateString
        }
        
        Log.d("StatisticViewModel", "Day filter result: ${filtered.size} workouts found")
        return filtered
    }
    
    fun getFilterText(): String {
        return when (_workoutFilterType.value) {
            WorkoutFilterType.ALL -> "Все"
            WorkoutFilterType.WEEK -> "За неделю"
            WorkoutFilterType.DAY -> "За день"
        }
    }
    
    // Методы для работы с месячными данными
    private fun getMonthStart(selectedDate: LocalDate): LocalDate {
        return selectedDate.withDayOfMonth(1)
    }
    
    private fun getMonthEnd(selectedDate: LocalDate): LocalDate {
        return selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
    }
    
    private fun loadMonthlyCalories() = viewModelScope.launch {
        val statisticList = statisticInteractor.getStatistic()
        val monthStart = getMonthStart(_selectedDate.value)
        val monthEnd = getMonthEnd(_selectedDate.value)
        
        // Фильтруем статистику за выбранный месяц
        val monthStatisticList = statisticList.filter { statistic ->
            val date = LocalDate.parse(statistic.date, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            date.isAfter(monthStart.minusDays(1)) && date.isBefore(monthEnd.plusDays(1))
        }
        
        // Группируем по неделям
        val monthlyData = mutableListOf<MonthlyCaloriesModel>()
        var currentWeekStart = monthStart
        
        while (currentWeekStart.isBefore(monthEnd.plusDays(1))) {
            val currentWeekEnd = currentWeekStart.plusDays(6).coerceAtMost(monthEnd)
            
            // Суммируем калории за неделю
            val weekCalories = monthStatisticList.filter { statistic ->
                val date = LocalDate.parse(statistic.date, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                date.isAfter(currentWeekStart.minusDays(1)) && date.isBefore(currentWeekEnd.plusDays(1))
            }.sumOf { it.kcal }
            
            // Формируем диапазон недели
            val weekRange = "${currentWeekStart.dayOfMonth}-${currentWeekEnd.dayOfMonth} ${getMonthName(currentWeekStart.monthValue)}"
            
            monthlyData.add(
                MonthlyCaloriesModel(
                    weekNumber = monthlyData.size + 1,
                    weekRange = weekRange,
                    calories = weekCalories.toInt(),
                    weekStart = TimeUtils.formatLocalDate(currentWeekStart),
                    weekEnd = TimeUtils.formatLocalDate(currentWeekEnd)
                )
            )
            
            currentWeekStart = currentWeekStart.plusDays(7)
        }
        
        _monthlyCalories.value = monthlyData
    }
    
    private fun getMonthName(monthValue: Int): String {
        val months = arrayOf("янв", "фев", "мар", "апр", "май", "июн", 
                         "июл", "авг", "сен", "окт", "ноя", "дек")
        return months[monthValue - 1]
    }
    
    fun toggleCalendarPeriod() = viewModelScope.launch {
        _calendarPeriod.value = if (_calendarPeriod.value == CalendarPeriod.WEEK) {
            CalendarPeriod.MONTH
        } else {
            CalendarPeriod.WEEK
        }
        
        // Перезагружаем данные в зависимости от периода
        if (_calendarPeriod.value == CalendarPeriod.WEEK) {
            loadWeeklyCalories()
        } else {
            loadMonthlyCalories()
        }
    }
}
