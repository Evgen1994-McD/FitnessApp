package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.selectedExerciseList.adapter.SelectedListExerciseAdapter
import com.example.fitnessapp.databinding.FragmentSelectedExerciseListBinding
import com.example.fitnessapp.db.ExerciseModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.util.Collections

@AndroidEntryPoint
class SelectedExerciseListFragment : Fragment(), SelectedListExerciseAdapter.Listener {
private var dayId = -1
    private var binding: FragmentSelectedExerciseListBinding? = null
    private val _binding get() = binding!!
    private lateinit var adapter: SelectedListExerciseAdapter

    private val model: SelectedExerciseListViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectedExerciseListBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        dayObserver()
        getArgs()
        _binding.addExercises.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("day_id", dayId)
            }
            findNavController().navigate(R.id.chooseExercisesFragment, bundle)
        }
    }

    private fun dayObserver(){
        model.exerciseData.observe(viewLifecycleOwner){ list ->
_binding.textEmpty.visibility = if(list.isEmpty()){
    View.VISIBLE
} else {
    View.GONE

}
            val count = "${getString(R.string.selected_exercise_count)} ${list.size}"
            _binding.tvExCount.text = count
            adapter.submitList(list)
        }
    }

    private fun getArgs(){
        arguments.apply {
             dayId = this?.getInt("day_id") ?: -1
             val dayNumber = this?.getInt("day_number") ?: -1
            (requireActivity() as AppCompatActivity).
                supportActionBar?.title = "${getString(R.string.day)} ${dayNumber}"
            if(dayId != -1){
model.getExercises(dayId)
            }
        }

        /*
        Получили аргументы дня который отправили с предыдущего фрагмента
        По id во вью модел получаем день из БД и этот день
        передаём на фрагмент через Лайв Дата
         */
    }


    private fun initRcView() {
        _binding.apply {
            rcView.layoutManager = LinearLayoutManager(requireContext())
            adapter = SelectedListExerciseAdapter(this@SelectedExerciseListFragment)
            rcView.adapter = adapter

            createItemTouchHelper().attachToRecyclerView(rcView)
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) { /*
        с помощью ItemTouchHelper можем как перетаскивать вверх - вниз, так и свайпать элементы
        В данном случае нас интересует только верх - низ.
        Мы будем использовать функцию onMove, где между вью холдерами и таргет вью холдером
        будем менять и перемешивать элементы.

        Элементы для перемешивания берем как Адаптер. текущий лист
       */
            override fun onMove(
                recyclerView: RecyclerView,
                startItem: RecyclerView.ViewHolder,
                targetItem: RecyclerView.ViewHolder,
            ): Boolean {
                val tempList = ArrayList<ExerciseModel>(adapter.currentList)
                Collections.swap(tempList, startItem.adapterPosition, targetItem.adapterPosition)
                adapter.submitList(tempList)
                return true

            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) {

            }

        }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
updateDay()
        binding = null
    }

    private fun updateDay(){
        var exercises = ""
        adapter.currentList.forEach {
            exercises += ",${it.id}"
        }
        model.updateDay(exercises)
    }


    override fun onDelete(pos:Int) {
        val tempList = ArrayList<ExerciseModel>(adapter.currentList)
        tempList.removeAt(pos)
        adapter.submitList(tempList)
        if (tempList.isEmpty()){
            _binding.textEmpty.visibility = View.VISIBLE
        }
    }

     fun setExerciseTime(pos:Int) {
         /*
         функция для настройки времени упражнений ( кастом)
          */
        val tempList = ArrayList<ExerciseModel>(adapter.currentList)
       val selectedExercise = let {  tempList[pos]}
        var replacerWithoutX =""
        var upX2 =""
         var stringTime = ""
        Log.d("MyLog", "Selected id = ${selectedExercise.id}")
        if (selectedExercise.time.startsWith("x")) {
            replacerWithoutX = ((selectedExercise.time).split("x"))[1]
            upX2 =  (replacerWithoutX.toInt()*2).toString()
            stringTime = "x$upX2"
        } else {
            replacerWithoutX = selectedExercise.time
            upX2 = (replacerWithoutX.toInt()*2).toString()
            stringTime = upX2

        }

Log.d("MyLog", stringTime)
val newEx = selectedExercise.copy(time = stringTime)
        runBlocking {
            model.saveNewExerciseAndReplace(selectedExercise, newEx)
        }
        adapter.submitList(tempList)
        runBlocking {
            model.getExercises(dayId)

        }
model.getExercises(dayId)
        if (tempList.isEmpty()){
            _binding.textEmpty.visibility = View.VISIBLE
        }
    }


    /*
    В данной функции прорабатываю изменение количества выполнений упражнений в своей
    тренировке.
    !
    !
    !

     */
}