package com.example.fitnessapp.statistic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.MainDb
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
import com.example.fitnessapp.statistic.ui.models.generateWeekDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

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
            .filter { it.isDone && it.completedDate != null }
            .sortedByDescending { it.completedDate }
            .take(10) // Показываем последние 10 тренировок
            
        val exerciseList = statisticInteractor.getAllExercise()
        val workoutHistoryList = allDays.mapNotNull { day ->
            val exercises = getExercisesFromIds(day.exercises, exerciseList)
            WorkoutHistoryModel(
                id = day.id,
                date = day.completedDate ?: "",
                zone = day.zone,
                difficulty = day.difficulty,
                caloriesBurned = 0.0, // Будет получено из StatisticModel
                duration = 0, // Будет получено из StatisticModel
                exercises = exercises
            )
        }
        _workoutHistory.value = workoutHistoryList
    }
    
    private fun loadWeeklyCalories() = viewModelScope.launch {
        val statisticList = statisticInteractor.getStatistic()
        val currentDate = LocalDate.now()
        val weekStart = currentDate.minusDays(6) // Последние 7 дней
        
        val weeklyData = (0..6).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val dateString = TimeUtils.getCurrentDate() // TODO: Заменить на форматирование LocalDate
            val dayStatistic = statisticList.find { it.date == dateString }
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
                calories = dayStatistic?.kcal?.toInt() ?: 0,
                dayNumber = dayOffset
            )
        }
        _weeklyCalories.value = weeklyData
    }
    
    private fun updateCalendarDays() {
        _calendarDays.value = generateWeekDays(_selectedDate.value)
    }
    
    fun onCalendarDayClick(day: DayCalendarModel) {
        _selectedDate.value = LocalDate.now().withDayOfMonth(day.dayNumber)
        updateCalendarDays()
        // Загружаем статистику для выбранной даты
        getStatisticByDate(TimeUtils.getCurrentDate()) // TODO: Обновить для LocalDate
    }
    
    fun toggleWorkoutExpanded(workoutId: Int) {
        val currentList = _workoutHistory.value.toMutableList()
        val workoutIndex = currentList.indexOfFirst { it.id == workoutId }
        if (workoutIndex != -1) {
            currentList[workoutIndex] = currentList[workoutIndex].copy(
                isExpanded = !currentList[workoutIndex].isExpanded
            )
            _workoutHistory.value = currentList
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
        
        // Сохраняем вес с ростом
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
        
        // Обновляем BMI данные
        loadBMIData()
        getWeightByYearAndMonth()
    }

}