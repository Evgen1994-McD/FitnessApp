package com.example.fitnessapp.customTraining.ui.chooseExercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentChooseExercisesBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.exercises.ui.compose.ExerciseBottomSheet
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseExercisesFragment : Fragment(), ChooseExercisesAdapter.Listener {
    private var newExercises = ""
    private lateinit var adapter: ChooseExercisesAdapter
    private var binding: FragmentChooseExercisesBinding? = null
    private val _binding get() = binding!!

    private val model: ChooseExercisesViewModel by viewModels()
    
    // Состояние фильтров
    private val selectedZones = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChooseExercisesBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Настройка кнопок фильтра
        setupFilterButtons()
        
        _binding.doneButton.setOnClickListener {
            model.updateDay(newExercises)
            findNavController().popBackStack()
        }
        getArgs()
        initRcView()
        exerciseListObserver()
        model.getAllExercises()
    }

    private fun setupFilterButtons() = with(_binding) {
        // Кнопка "Спина"
        filterBack.setOnClickListener {
            toggleFilter("back", filterBack)
        }
        
        // Кнопка "Руки"
        filterHands.setOnClickListener {
            toggleFilter("hands", filterHands)
        }
        
        // Кнопка "Ноги"
        filterLegs.setOnClickListener {
            toggleFilter("legs", filterLegs)
        }
        
        // Кнопка "Тело"
        filterBody.setOnClickListener {
            toggleFilter("body", filterBody)
        }
    }
    
    private fun toggleFilter(zone: String, button: View) {
        if (selectedZones.contains(zone)) {
            selectedZones.remove(zone)
            button.setBackgroundColor(resources.getColor(R.color.black_light))
        } else {
            selectedZones.add(zone)
            button.setBackgroundColor(resources.getColor(R.color.blue))
        }
        
        // Применяем фильтр
        applyFilter()
    }
    
    private fun applyFilter() {
        model.exerciseListData.value?.let { exercises ->
            val filtered = if (selectedZones.isEmpty()) {
                exercises
            } else {
                exercises.filter { exercise ->
                    exercise.muscleZone?.split(",")?.any { zone ->
                        selectedZones.contains(zone.trim())
                    } == true
                }
            }
            adapter.submitList(filtered)
        }
    }

    private fun initRcView() = with(_binding) {
        rcView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChooseExercisesAdapter(this@ChooseExercisesFragment)
        rcView.adapter = adapter
    }

    private fun exerciseListObserver() {
        model.exerciseListData.observe(viewLifecycleOwner) { list ->
            applyFilter() // Применяем фильтр при получении новых данных
        }
    }

    private fun getArgs() {
        arguments.apply {
            val dayId = this?.getInt("day_id") ?: -1

            if (dayId != -1) {
                model.getDayById(dayId)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(exercise: ExerciseModel) {
        if (exercise.id != -1) {
            newExercises += ",${exercise.id}"
        }
        val count = newExercises.split(",").size - 1
        val choosenCounterText = "${getString(R.string.selected_exercise_count)} $count"
        _binding.tvChoosenExCounter.text = choosenCounterText

    }

    override fun onLongClick(exercise: ExerciseModel) {
        showExerciseBottomSheet(exercise)
    }

    override fun onInfoClick(exercise: ExerciseModel) {
        showExerciseBottomSheet(exercise)
    }

    private fun showExerciseBottomSheet(exercise: ExerciseModel) {
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

}

