package com.example.fitnessapp.customTraining.ui.chooseExercises

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.customTraining.domain.CustomInteractor
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
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

    fun updateDay(exercises: String) = viewModelScope.launch {
        val oldExercises = dayModel?.exercises ?: ""
        val tempExercises = if(oldExercises.isEmpty()){
            exercises.replaceFirst(",", "")
        } else {
            exercises
        }
        dayModel?.copy(
            exercises = oldExercises + tempExercises
        )?.let {
            customInteractor.insertDay(it)

        }
    }

}