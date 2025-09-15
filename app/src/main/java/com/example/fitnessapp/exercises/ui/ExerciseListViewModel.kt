package com.example.fitnessapp.exercises.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.domain.ExerciseInteractor
import com.example.fitnessapp.training.data.TrainingTopCardModel
import com.example.fitnessapp.training.utils.TrainingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

/* для каждого объекта будет свой вью модел */
@HiltViewModel //анннотация Хилт вьюмодел
class ExerciseListViewModel @Inject constructor( // инжект для того, чтобы передать конструктор с базой данных
    private val exerciseInteractor: ExerciseInteractor  // Daggger подключил нам базу данных с помощью которой можем получать список
) : ViewModel() {
    companion object{

        const val EASY = "easy"
        const val MIDDLE = "middle"
        const val HARD = "hard"
        const val CUSTOM = "custom"
    }

    val exerciseList =
        MutableLiveData<List<ExerciseModel>>() // сюда мы с базы данных будем передавать данные, а затем получать список с помощью обсервера уже на фрагменте
    val topCardUpdate = MutableLiveData<TrainingTopCardModel>()

    fun getDayExerciseList(dayModel: DayModel?) =
        viewModelScope.launch { // запускаем в корутине, потому что сложная операция
            val day =
                dayModel?.id?.let { exerciseInteractor.getDayById(it) } // выбарается самый свежий день из базы данных по id
            if (day != null) {
                getTopCardData(day)
            }
            val allExerciseList =
               exerciseInteractor.getAllExerciseList() // получаем список со всеми упражнениями который будем потом перебирать
            val tempExerciseList = ArrayList<ExerciseModel>()
            day?.let { dayModel ->
                dayModel.exercises.split(",").forEach { id ->
                    if (id.isNullOrEmpty()) return@forEach //Выходим из цикла если id пуст или null
                    tempExerciseList.add(allExerciseList.filter {
                        it.id == id.toInt()
                    }[0])  // сюда добавляем номера упражнений данного дня, а ЗАТЕМ по номеру достаём нужные из allExrciseList и помещать в новый список (exerciseList)
                } // как в первой части мы разделяем по запятой, в результате получим массив в виде объекта String
                if (dayModel.doneExerciseCounter>0) {
                    for (i in 0..<dayModel.doneExerciseCounter) {
                        tempExerciseList[i] = tempExerciseList[i].copy(isDone = true)
                        /*
                    Выше мы определили выполненные упражнения. А с помощью цикла for
                    перебрали эти упражнения и изменили значение переменной isDone
                    чтобы показать галочку ( Выполнено)
                     */
                    }
                }
                    exerciseList.value = tempExerciseList // передали упражнения

            }


        }

    /* теперь остаётся все передать в функцию и поставить обсервер чтобы следить за изменениями. Когда переберём списко - сразу выдаст этот список и передадим его уже в адаптер */
    fun getTopCardData(dayModel: DayModel) {
        var index = 0
        when (dayModel.difficulty) {
            "middle" -> {
                index = 1
            }

            "hard" -> {
                index = 2
            }

            else -> index = 0
        }
        topCardUpdate.value = TrainingUtils.topCardList[index].copy(
            progress = dayModel.doneExerciseCounter,
            maxProgress = dayModel.exercises.split(",").size
        ) /*
                Передаю карточку по позиции, которую определили выше. Позицию перевели в Int - чтобы взять нужную карточку из массива
              progress - вычисллили текущий прогресс ( он будет в базе данных, а передаётся совместно с dayModel
              maxProgress - вычислили, путём разделения массива переданного daymodel по запятой с помощью метода split, и узнав
              его размер с помощью метода массива .size


              */
    }
}