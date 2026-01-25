package com.example.fitnessapp.exercises.ui.days

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.domain.DaysInteractor
import com.example.fitnessapp.exercises.domain.models.TrainingTopCardModel
import com.example.fitnessapp.exercises.utils.TrainingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel //пометили аннотацией что это модел Хилт
// будем вызывать БД из МейнМодел
class DaysViewModel @Inject constructor(
    private val daysInteractor: DaysInteractor, // получили доступ к базе данных
) : ViewModel() { //если БД инициализирована мы её найдём  в МейнМодуль
    companion object {
const val CUSTOM ="custom"
    }



    val daysList = MutableLiveData<List<DayModel>>() // список дней с тренировками
    val topCardUpdate = MutableLiveData<TrainingTopCardModel>()
    val isCustomListEmpty = MutableLiveData<Boolean>()
    val allBodyTrainingDays = MutableLiveData<List<DayModel>>(emptyList())
    val difficultyProgressMap = MutableLiveData<Map<String, Pair<Int, Int>>>(emptyMap()) // Map<difficulty, Pair<progress, maxProgress>>
    val allBodyProgressMap = MutableLiveData<Map<String, TrainingTopCardModel>>(emptyMap()) // Map<difficulty, TrainingTopCardModel>

    fun getExerciseDaysByDifficulty ( trainingTopCardModel: TrainingTopCardModel) {
        viewModelScope.launch {  /* это трудоёмкая операция, поэтому делаем
        в корутинах */
            daysInteractor.getExerciseDaysByDifficulty(trainingTopCardModel.difficulty).collect { /* collect -
            получить то что найдём в БД */
                list ->

daysList.value = list // передали лист который нашли
                topCardUpdate.value = trainingTopCardModel.copy(
                    maxProgress = list.size,
                    progress = getProgress(list)

                )
            }

        }
    }

    fun getCustomDaysList() = viewModelScope.launch {
        daysInteractor.getExerciseDaysByDifficulty(CUSTOM).collect {
            isCustomListEmpty.value = it.isEmpty()
        }
        /*
        у нас Flow - поэтому мы делаем collect ( это не просто список)
         */
    }

    fun getAllBodyTrainingDays() {
        viewModelScope.launch {
            val difficulties = listOf(TrainingUtils.EASY, TrainingUtils.MIDDLE, TrainingUtils.HARD, CUSTOM)
            val difficultyMap = mutableMapOf<String, List<DayModel>>()
            val progressMap = mutableMapOf<String, Pair<Int, Int>>()
            val mutex = Mutex()
            
            // Запускаем сбор данных для каждой сложности параллельно
            difficulties.forEach { difficulty ->
                launch {
                    // Используем тот же метод, что и в getExerciseDaysByDifficulty
                    val topCardModel = TrainingUtils.topCardList.find { it.difficulty == difficulty }
                        ?: TrainingUtils.topCardList[0]
                    
                    daysInteractor.getExerciseDaysByDifficulty(difficulty).collect { list ->
                        // Фильтруем только тренировки на всё тело (zone == null)
                        val allBodyDays = list.filter { it.zone == null }
                        
                        // Используем тот же метод подсчета, что и в getExerciseDaysByDifficulty
                        // Берем готовый прогресс из topCardUpdate (как в строке 42-44)
                        val progress = getProgress(allBodyDays) // тот же метод, что в getExerciseDaysByDifficulty
                        val maxProgress = allBodyDays.size
                        
                        // Синхронизируем доступ к Map
                        mutex.withLock {
                            difficultyMap[difficulty] = allBodyDays
                            // Сохраняем прогресс (используем тот же метод подсчета, что и в topCardUpdate)
                            progressMap[difficulty] = Pair(progress, maxProgress)
                            
                            // Объединяем все тренировки на всё тело из всех сложностей
                            val allDays = difficultyMap.values.flatten()
                            allBodyTrainingDays.postValue(allDays)
                            
                            // Обновляем статистику (создаем новую копию Map для thread-safety)
                            difficultyProgressMap.postValue(HashMap(progressMap))
                        }
                    }
                }
            }
        }
    }

    fun loadAllBodyProgress() {
        viewModelScope.launch {
            val difficulties = listOf(TrainingUtils.EASY, TrainingUtils.MIDDLE, TrainingUtils.HARD, TrainingUtils.CUSTOM)
            val progressMap = mutableMapOf<String, TrainingTopCardModel>()
            val mutex = Mutex()
            
            // Инициализируем Map начальными значениями для каждой сложности
            difficulties.forEach { difficulty ->
                val topCardModel = TrainingUtils.topCardList.find { it.difficulty == difficulty }
                    ?: TrainingUtils.topCardList[0]
                progressMap[difficulty] = topCardModel.copy(progress = 0, maxProgress = 0)
            }
            // Сразу отправляем начальные значения
            allBodyProgressMap.postValue(HashMap(progressMap))
            
            // Запускаем сбор данных для каждой сложности параллельно
            difficulties.forEach { difficulty ->
                launch {
                    val topCardModel = TrainingUtils.topCardList.find { it.difficulty == difficulty }
                        ?: TrainingUtils.topCardList[0]
                    
                    daysInteractor.getExerciseDaysByDifficulty(difficulty).collect { list ->
                        // Фильтруем только тренировки на всё тело (zone == null)
                        val allBodyDays = list.filter { it.zone.isNullOrEmpty() }
                        
                        // Используем тот же метод подсчета, что и в getExerciseDaysByDifficulty
                        val progress = getProgress(allBodyDays)
                        val maxProgress = allBodyDays.size
                        
                        // Синхронизируем доступ к Map
                        mutex.withLock {
                            progressMap[difficulty] = topCardModel.copy(
                                progress = progress,
                                maxProgress = maxProgress
                            )
                            // Обновляем Map (создаем новую копию для thread-safety)
                            allBodyProgressMap.postValue(HashMap(progressMap))
                        }
                    }
                }
            }
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