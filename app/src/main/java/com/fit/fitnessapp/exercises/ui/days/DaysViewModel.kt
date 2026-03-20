package com.fit.fitnessapp.exercises.ui.days

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.exercises.domain.DaysInteractor
import com.fit.fitnessapp.exercises.domain.models.TrainingTopCardModel
import com.fit.fitnessapp.exercises.utils.TrainingUtils
import com.fit.fitnessapp.statistic.domain.StatisticInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel //пометили аннотацией что это модел Хилт
// будем вызывать БД из МейнМодел
class DaysViewModel @Inject constructor(
    private val daysInteractor: DaysInteractor, // получили доступ к базе данных
    private val statisticInteractor: StatisticInteractor // добавили доступ к статистике
) : ViewModel() { //если БД инициализирована мы её найдём  в МейнМодуль

    companion object {

        val difficulties = listOf(TrainingUtils.EASY, TrainingUtils.MIDDLE, TrainingUtils.HARD, TrainingUtils.CUSTOM)
    }


    val zoneTrainingList = MutableLiveData<List<DayModel>>() // список дней с тренировками

    val daysList = MutableLiveData<List<DayModel>>() // список дней с тренировками
    val topCardUpdate = MutableLiveData<TrainingTopCardModel>()
    val allBodyProgressMap = MutableLiveData<Map<String, TrainingTopCardModel>>(emptyMap()) // Map<difficulty, TrainingTopCardModel>
    
    // Новые LiveData для общей статистики
    val totalWorkouts = MutableLiveData<Int>(0)
    val totalKcal = MutableLiveData<Int>(0)
    val totalTime = MutableLiveData<String>("00:00")

    fun getExerciseDaysByDifficulty ( trainingTopCardModel: TrainingTopCardModel, zone: String? = null) {
        viewModelScope.launch {  /* это трудоёмкая операция, поэтому делаем
        в корутинах */
            daysInteractor.getExerciseDaysByDifficulty(trainingTopCardModel.difficulty).collect { /* collect -
            получить то что найдём в БД */
                list ->

                val filteredList = if (zone.isNullOrEmpty()) {
                    list.filter { it.zone.isNullOrEmpty() }
                } else {
                    list.filter { it.zone == zone }
                }

                // Просто используем значения isOpen из базы данных
                // isOpen = 0 → закрыто (замочек), isOpen = 1 → открыто
                val processedList = filteredList.map { day ->
                    day // Используем значения из базы как есть
                }

                daysList.value = processedList // передали лист который нашли
                
                val imageRes = TrainingUtils.getTrainingImage(trainingTopCardModel.difficulty, zone)
                
                topCardUpdate.value = trainingTopCardModel.copy(
                    imageId = imageRes,
                    maxProgress = processedList.size,
                    progress = getProgress(processedList)

                )
            }
        }
    }

    // Метод для принудительного обновления всех данных дней (после открытия всех тренировок)
    fun refreshAllDays() {
        loadAllBodyProgress()
    }


    fun loadAllBodyProgress() {
        loadOverallStatistics()
        viewModelScope.launch {
            val progressMap = mutableMapOf<String, TrainingTopCardModel>()
            val mutex = Mutex()

            // Определяем все зоны
            val zones = listOf(
                "",  // Для всех зон вместе
                TrainingUtils.HANDS,
                TrainingUtils.BODY,
                TrainingUtils.BACK,
                TrainingUtils.LEGS,
                TrainingUtils.CHEST,
                TrainingUtils.ABS
            )

            // Инициализируем Map для каждой сложности и каждой зоны
            difficulties.forEach { difficulty ->
                zones.forEach { zone ->
                    val topCardModel = TrainingUtils.topCardList.find { it.difficulty == difficulty }
                        ?: TrainingUtils.topCardList[0]
                    val key = if (zone == "") difficulty else "${difficulty}_$zone"
                    progressMap[key] = topCardModel.copy(
                        progress = 0,
                        maxProgress = 0,
                        title = if (zone == "") topCardModel.title else "${zone} ${topCardModel.title}"
                    )
                }
            }

            // Сразу отправляем начальные значения
            allBodyProgressMap.postValue(HashMap(progressMap))

            // Запускаем сбор данных для каждой сложности
            difficulties.forEach { difficulty ->
                launch {
                    daysInteractor.getExerciseDaysByDifficulty(difficulty).collect { list ->
                        zones.forEach { zone ->
                            // Фильтруем по зоне
                            val filteredList = when (zone) {
                                "" -> list.filter { it.zone.isNullOrEmpty() }
                                else -> list.filter { it.zone == zone }
                            }

                            val progress = getProgress(filteredList)
                            val maxProgress = filteredList.size

                            // Создаем обновленную модель
                            val topCardModel = TrainingUtils.topCardList.find { it.difficulty == difficulty }
                                ?: TrainingUtils.topCardList[0]

                            val imageRes = TrainingUtils.getTrainingImage(difficulty, if(zone == "") null else zone)

                            val updatedModel = topCardModel.copy(
                                imageId = imageRes,
                                progress = progress,
                                maxProgress = maxProgress,
                                title = if (zone.isNullOrEmpty()) topCardModel.title else "${zone} ${topCardModel.title}"
                            )

                            // Формируем ключ
                            val key = if (zone.isNullOrEmpty()) difficulty else "${difficulty}_$zone"

                            // Обновляем Map
                            mutex.withLock {
                                progressMap[key] = updatedModel
                                allBodyProgressMap.postValue(HashMap(progressMap))
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadOverallStatistics() {
        viewModelScope.launch {
            val statisticList = statisticInteractor.getStatistic()
            var totalKcalValue = 0.0
            var totalSeconds = 0L

            statisticList.forEach {
                totalKcalValue += it.kcal
                totalSeconds += it.workoutTime.toLongOrNull() ?: 0L
            }

            totalWorkouts.postValue(statisticList.size)
            totalKcal.postValue(totalKcalValue.toInt())

            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            val timeString = if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
            totalTime.postValue(timeString)
        }
    }

    private fun getProgress(list:List<DayModel>): Int {
        var counter = 0
        list.forEach { day ->
            if (day.isDone){
                counter++
            } // Подсчитаем сколько дней уже выполнено. Если выполнено - то Counter +1, для прогресс бара
        }
        return counter // таким образом узнали прогресс
    }

     fun resetSelectedDay(day: DayModel) = viewModelScope.launch {
         var tempDay = day.copy(
             doneExerciseCounter = 0,
             isDone = false
         )
     daysInteractor.insertDay(tempDay)

    }
}