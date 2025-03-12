package com.example.fitnessapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DayModel
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.adapter.ExerciseModel
import com.example.fitnessapp.databinding.FragmentDaysBinding
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.MainViewModel
import java.util.zip.Inflater

class DaysFragment : Fragment(), DaysAdapter.Listener { // Подключили интерфейс из который создали в DaysAdapter
    private lateinit var binding: FragmentDaysBinding
    private val model: MainViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private var ab: ActionBar? = null // добавили переменную для ActionBar, будем показывать счетчик упражнений

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.trainingDays)

        super.onViewCreated(view, savedInstanceState)
   initRcView()
    }


    private fun initRcView() = with(binding){
        val adapter = DaysAdapter(this@DaysFragment)
        rcviewdays.layoutManager = LinearLayoutManager(activity as AppCompatActivity)
        rcviewdays.adapter = adapter
        adapter.submitList(fillDaysArray())
    }


    private fun fillDaysArray() : ArrayList<DayModel>{
        val tArray = ArrayList<DayModel>() // создаём класс для заполнения из массивов с упражнениями. получается массив с упражнениями который состоит из DayModel
    resources.getStringArray(R.array.day_exercise).forEach {
    tArray.add(DayModel(it, false))
    }
    return tArray
    }

    private fun fillExerciseList(day: DayModel) {
        val templist = ArrayList<ExerciseModel>()
        day.exercises.split(",").forEach {//Мы вызвали опять массив с упражнениями, разбили его по запятой. Далее - с помощью фор ич получаем конкретное число которое далее используем для получения упражнения
           val exerciseList = resources.getStringArray(R.array.exercise)  // Внутри цикла forEach получаем упражнения из массива, которое далее тоже разделим на 3 части
           val exercise = exerciseList[it.toInt()] // здесь уже переводим It который получили выше в ИНТ и получаем элемент из массива с упражнениями. Далее мы тоже разделим его по палочке и получим элементы упражнения
        val exerciseArray = exercise.split("|")    // теперь элементы упражнения будут по порядку по позициям. Название, время. Картинка
        templist.add(ExerciseModel(exerciseArray[0], exerciseArray[1], exerciseArray[2])) // мы заполнили ExerciseModel.
        }// В итоге когда пройдёт весь список, у нас будут заполнены все упражнения в templist !

    model.mutableListExercise.value = templist // Везде где будет подключен "Обсервер", где подключен ViewModel, будет передаваться список из наших упражнений

    }



    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()


    }

    override fun onClick(day: DayModel) {  // функция интерфейса  //Фунцкия  перехода на фрагмент с упражнениями
        fillExerciseList(day)
        FragmentManager.setFragment(ExListFragment.newInstance(),
            activity as AppCompatActivity)
    }
}