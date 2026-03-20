package com.fit.fitnessapp.customTraining.ui.chooseExercises

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit.fitnessapp.customTraining.domain.CustomInteractor
import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseExercisesViewModel @Inject constructor(
    private val customInteractor: CustomInteractor
) : ViewModel() {
    companion object{
        const val from = 4
        const val to = 174
        /*
        Максимальное / минимальное упражнение в базе данных
         */
    }

    val exerciseListData = MutableLiveData<List<ExerciseModel>>()
private var dayModel: DayModel? = null
    fun getAllExercises() = viewModelScope.launch {
        exerciseListData.value = customInteractor.getAllExercisesFromTo(from, to)
    }

    fun getExercisesByZone(zone: String) = viewModelScope.launch {
        exerciseListData.value = customInteractor.getExercisesByZone(from, to, zone)
    }

    fun getAllExercisesSorted() = viewModelScope.launch {
        exerciseListData.value = customInteractor.getAllExercisesSorted(from, to)
    }

    fun getDayById(id: Int)= viewModelScope.launch {
        dayModel = customInteractor.getDayById(id)
    }


    fun insertCustomExercise(exercise: ExerciseModel, callback: (Long) -> Unit) = viewModelScope.launch {
        try {
            val newId = customInteractor.insertExercise(exercise)
            callback(newId)
        } catch (e: Exception) {
            callback(-1L)
        }
    }

    fun updateExerciseFavorite(exercise: ExerciseModel) = viewModelScope.launch {
        try {
            customInteractor.updateExercise(exercise)
            android.util.Log.d("ChooseExercisesViewModel", "Упражнение ${exercise.name} обновлено. isFavorite: ${exercise.isFavorite}")
        } catch (e: Exception) {
            android.util.Log.e("ChooseExercisesViewModel", "Ошибка при обновлении упражнения", e)
        }
    }

    fun updateDay(exercises: String) = viewModelScope.launch {
        val oldExercises = dayModel?.exercises ?: ""
        android.util.Log.d("ChooseExercisesViewModel", "Обновление дня. Старые упражнения: '$oldExercises', новые: '$exercises'")
        
        val finalExercises = if (oldExercises.isEmpty()) {
            // Если старых упражнений нет, просто используем новые
            exercises
        } else if (exercises.isEmpty()) {
            // Если новых упражнений нет, оставляем старые
            oldExercises
        } else {
            // Добавляем новые упражнения к старым через запятую
            val oldExercisesList = oldExercises.split(",").filter { it.isNotBlank() }
            val newExercisesList = exercises.split(",").filter { it.isNotBlank() }
            
            // Объединяем списки и удаляем дубликаты
            val combinedExercises = (oldExercisesList + newExercisesList).distinct()
            combinedExercises.joinToString(",")
        }
        
        android.util.Log.d("ChooseExercisesViewModel", "Итоговые упражнения для сохранения: '$finalExercises'")
        
        dayModel?.copy(
            exercises = finalExercises
        )?.let {
            customInteractor.insertDay(it)
            android.util.Log.d("ChooseExercisesViewModel", "День обновлен с упражнениями: '$finalExercises'")
        }
    }

}