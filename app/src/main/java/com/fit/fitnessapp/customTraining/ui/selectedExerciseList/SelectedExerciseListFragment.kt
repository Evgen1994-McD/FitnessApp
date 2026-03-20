package com.fit.fitnessapp.customTraining.ui.selectedExerciseList

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fit.fitnessapp.R
import com.fit.fitnessapp.databinding.FragmentSelectedExerciseListBinding
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.exercises.ui.compose.ExerciseBottomSheet
import com.fit.fitnessapp.ui.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.util.Collections
import kotlin.math.roundToInt

@AndroidEntryPoint
class SelectedExerciseListFragment : Fragment(), SelectedListExerciseAdapter.Listener {
    private var dayId = -1
    private var binding: FragmentSelectedExerciseListBinding? = null
    private val _binding get() = binding!!
    private lateinit var adapter: SelectedListExerciseAdapter
    private lateinit var tempList: ArrayList<ExerciseModel>

    private val model: SelectedExerciseListViewModel by viewModels()
    private var isBottomSheetShowing = false // Флаг для дебаунса

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
        
        // Обработчик для кнопки Start
        _binding.start.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("day_id", dayId)
            }
            findNavController().navigate(R.id.exListFragment, bundle)
        }
    }

    private fun dayObserver(){
        model.exerciseData.observe(viewLifecycleOwner){ list ->
            val isEmpty = list.isEmpty()
            
            // Управляем видимостью плейсхолдера и текстов
            _binding.textEmpty.visibility = if(isEmpty) View.VISIBLE else View.GONE
            _binding.imageEmpty.visibility = if(isEmpty) View.VISIBLE else View.GONE
            _binding.textEmptySubtext.visibility = if(isEmpty) View.VISIBLE else View.GONE

            // Управляем видимостью кнопки Start
            _binding.start.visibility = if(isEmpty) View.GONE else View.VISIBLE

            val count = "${getString(R.string.selected_exercise_count)} ${list.size}"
            _binding.tvExCount.text = count
            adapter.submitList(list)
        }
        
        model.isLoading.observe(viewLifecycleOwner) { isLoading ->
            _binding.progressLoading.visibility = if(isLoading) View.VISIBLE else View.GONE
            
            // Во время загрузки скрываем плейсхолдер
            if (isLoading) {
                _binding.textEmpty.visibility = View.GONE
                _binding.imageEmpty.visibility = View.GONE
                _binding.textEmptySubtext.visibility = View.GONE
                _binding.start.visibility = View.GONE
            }
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
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT 
        ) { /*
        с помощью ItemTouchHelper можем как перетаскивать вверх - вниз, так и свайпать элементы
        В данном случае нас интересует и верх-низ, и свайп для удаления.
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
                // Удаляем элемент при свайпе
                val position = viewHolder.adapterPosition
                deleteExercise(position)
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

        Log.d("MyLog", " Update exercises = $exercises")
        model.updateDay(exercises)

    }


    private fun deleteExercise(position: Int) {
        tempList = ArrayList<ExerciseModel>(adapter.currentList)
        var exercises = ""

        tempList.removeAt(position)

        Log.d("MyLog", "TempListOnDelete = ${tempList}")
        tempList.forEach {
            exercises += ",${it.id}"
        }
        runBlocking {
            model.updateDay(exercises)
        }
        runBlocking {
            model.getExercises(dayId)
        }
        adapter.submitList(tempList)

        // Видимость управляется в dayObserver, дублирование не нужно
    }

    override fun onInfoClick(exercise: ExerciseModel) {
        showExerciseBottomSheet(exercise)
    }

    private fun showExerciseBottomSheet(exercise: ExerciseModel) {
        // Проверяем флаг дебаунса
        if (isBottomSheetShowing) {
            return
        }
        
        isBottomSheetShowing = true
        
        val composeView = ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setContent {
                FitnessAppTheme {
                    ExerciseBottomSheet(
                        exercise = exercise,
                        onDismiss = {
                            // Сбрасываем флаг при закрытии
                            isBottomSheetShowing = false
                            // Удаляем ComposeView из parent
                            (parent as? ViewGroup)?.removeView(this)
                        }
                    )
                }
            }
        }
        
        // Добавляем ComposeView в корневой layout
        _binding.root.addView(composeView)
    }

    override fun addExerciseTime(pos:Int) {
        try {
            // Инициализируем tempList текущими данными из адаптера
            tempList = ArrayList<ExerciseModel>(adapter.currentList)
            
            // Проверяем что позиция валидна
            if (pos < 0 || pos >= tempList.size) {
                Log.d("MyLog", "Неверный Индекс: $pos, размер списка: ${tempList.size}")
                return
            }
            
            val selectedExercise = tempList[pos].copy()
            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            Log.d("MyLog", "Selected id = ${selectedExercise.id}")
            if (selectedExercise.time.startsWith("x")) {
                replacerWithoutX = ((selectedExercise.time).split("x"))[1]
                upX2 = (replacerWithoutX.toInt() * 1.5).roundToInt().toString()
                stringTime = "x$upX2"
            } else {
                replacerWithoutX = selectedExercise.time
                upX2 = ((replacerWithoutX.toInt() * 1.5).roundToInt()).toString()
                stringTime = upX2

            }

            Log.d("MyLog", stringTime)
            val newEx = selectedExercise.copy(time = stringTime)
            runBlocking {
                model.saveNewExerciseAndReplace(newEx, pos)
            }
            runBlocking {
                model.getExercises(dayId)

            }
            // Видимость управляется в dayObserver, дублирование не нужно
        }
        catch (e: IndexOutOfBoundsException) {
            Log.d("MyLog", "Неверный Индекс: ${e.message}")
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Ошибка: Невозможно преобразовать строку в число.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Возникла неизвестная ошибка.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun decreaseExerciseTime(pos:Int) {
        /*
        функция для настройки времени упражнений ( кастом)
         */

        try {
            // Инициализируем tempList текущими данными из адаптера
            tempList = ArrayList<ExerciseModel>(adapter.currentList)
            
            // Проверяем что позиция валидна
            if (pos < 0 || pos >= tempList.size) {
                Log.d("MyLog", "Неверный Индекс: $pos, размер списка: ${tempList.size}")
                return
            }
            
            val selectedExercise = tempList[pos].copy()
            var replacerWithoutX =""
            var upX2 =""
            var stringTime = ""
            Log.d("MyLog", "Selected id = ${selectedExercise.id}")
            if (selectedExercise.time.startsWith("x")) {
                replacerWithoutX = ((selectedExercise.time).split("x"))[1]
             if (replacerWithoutX.toInt()/1.5 >0){
                upX2 = (replacerWithoutX.toInt()/1.5).roundToInt().toString()
            } else upX2 = "1"


            stringTime = "x$upX2"
        } else {
            replacerWithoutX = selectedExercise.time

            if (replacerWithoutX.toInt()/1.5 >0){
                upX2 = (replacerWithoutX.toInt()/1.5).roundToInt().toString()
            } else upX2 = "1"

            stringTime = upX2

        }

        Log.d("MyLog", stringTime)
        val newEx = selectedExercise.copy(time = stringTime)
        runBlocking {
            model.saveNewExerciseAndReplace( newEx, pos)
        }
        runBlocking {
            model.getExercises(dayId)

        }
        // Видимость управляется в dayObserver, дублирование не нужно
        }
        catch (e: IndexOutOfBoundsException) {
            Log.d("MyLog", "Неверный Индекс: ${e.message}")
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Ошибка: Невозможно преобразовать строку в число.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Возникла неизвестная ошибка.", Toast.LENGTH_SHORT).show()
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
