package com.example.fitnessapp.training.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.training.data.TrainingTopCardModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel //пометили аннотацией что это модел Хилт
// будем вызывать БД из МейнМодел
class DaysViewModel @Inject constructor(
    private val mainDb: MainDb, // получили доступ к базе данных
) : ViewModel() { //если БД инициализирована мы её найдём  в МейнМодуль

val daysList = MutableLiveData<List<DayModel>>() // список дней с тренировками
val topCardUpdate = MutableLiveData<TrainingTopCardModel>()

    fun getExerciseDaysByDifficulty ( trainingTopCardModel: TrainingTopCardModel) {
        viewModelScope.launch {  /* это трудоёмкая операция, поэтому делаем
        в корутинах */
            mainDb.daysDao.getAllDaysByDifficulty(trainingTopCardModel.difficulty).collect { /* collect -
            получить то что найдём в БД */
                list ->
                Log.d("MyLog", "Days list: ${list.size}")
daysList.value = list // передали лист который нашли
                topCardUpdate.value = trainingTopCardModel.copy(
                    maxProgress = list.size,
                    progress = getProgress(list)

                )
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
        return list.size - counter // таким образом узнали прогресс
    }
}