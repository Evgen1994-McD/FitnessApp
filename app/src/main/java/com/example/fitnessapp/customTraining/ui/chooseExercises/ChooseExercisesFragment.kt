package com.example.fitnessapp.customTraining.ui.chooseExercises

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.ui.chooseExercises.ZoneFilter
import com.example.fitnessapp.databinding.FragmentChooseExercisesBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.exercises.ui.compose.ExerciseBottomSheet
import com.example.fitnessapp.exercises.utils.TrainingUtils
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseExercisesFragment : Fragment(), ChooseExercisesAdapter.Listener {
    private var newExercises = ""
    private lateinit var adapter: ChooseExercisesAdapter
    private var binding: FragmentChooseExercisesBinding? = null
    private val _binding get() = binding as FragmentChooseExercisesBinding

    private val model: ChooseExercisesViewModel by viewModels()
    
    private var isBottomSheetShowing = false // Флаг для дебаунса
    private var allExercises = listOf<ExerciseModel>()
    private val selectedFilters = mutableSetOf<String>()  // Множество выбранных фильтров (английские названия для фильтрации)
    
    // Все зоны для фильтрации
    private val allZones = listOf(
        ZoneFilter("hands", "Руки", 0, false),
        ZoneFilter("body", "Общие", 0, false),
        ZoneFilter("chest", "Грудь", 0, false),
        ZoneFilter("shoulders", "Плечи", 0, false),
        ZoneFilter("back", "Спина", 0, false),
        ZoneFilter("legs", "Ноги", 0, false),
        ZoneFilter("abs", "Пресс", 0, false),
        ZoneFilter("warm", "Разминка", 0, false),
        ZoneFilter("stretch", "Растяжка", 0, false)
    )
    
    // Функция для перевода английских названий зон на русский
    private fun translateZoneName(englishName: String): String {
        return when (englishName.lowercase()) {
            "hands" -> "Руки"
            "body" -> "Общие"
            "chest" -> "Грудь"
            "shoulders" -> "Плечи"  // Исправлено на множественное число
            "shoulder" -> "Плечи"   // Добавлено на всякий случай
            "back" -> "Спина"
            "legs" -> "Ноги"
            "abs" -> "Пресс"
            "warm" -> "Разминка"
            "stretch" -> "Растяжка"
            else -> englishName // Если нет перевода, оставляем как есть
        }
    }

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
        
        // Настройка chips для фильтрации
        setupFilterChips()
        
        _binding.doneButton.setOnClickListener {
            model.updateDay(newExercises)
            findNavController().popBackStack()
        }
        getArgs()
        initRcView()
        exerciseListObserver()
        model.getAllExercises()
    }

    private fun setupFilterChips() = with(_binding) {
        // Создаем chips для всех зон
        allZones.forEach { zone ->
            val chip = Chip(requireContext(), null, R.style.CustomChipStyle).apply {
                text = zone.displayName  // Показываем русское название
                isCheckable = true
                chipIcon = null  // Убираем иконку
                isCloseIconVisible = false  // Убираем иконку закрытия

                val isSelected = selectedFilters.contains(zone.zoneName)
                isChecked = isSelected
                
                // Устанавливаем начальные цвета
                if (isSelected) {
                    // Выбранный чип: синий фон, белый текст
                    setChipBackgroundColorResource(R.color.blue)
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    // Невыбранный чип: стандартный фон, стандартный текст
                    setChipBackgroundColorResource(R.color.chip_background)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text))
                }
                
                setOnClickListener {
                    selectZone(zone)
                }
            }
            
            _binding.chipGroup.addView(chip)
        }
    }
    
    private fun selectZone(selectedZone: ZoneFilter) {
        Log.d("ChooseExercisesFragment", "selectZone: ${selectedZone.displayName} (${selectedZone.zoneName}), selectedFilters: $selectedFilters")
        
        // Добавляем или убираем фильтр из множества (используем английское название для фильтрации)
        val zoneName = selectedZone.zoneName
        if (selectedFilters.contains(zoneName)) {
            selectedFilters.remove(zoneName)
        } else {
            selectedFilters.add(zoneName)
        }
        
        applyCurrentFilter()
    }
    
    private fun updateChipStates() {
        for (i in 0 until _binding.chipGroup.childCount) {
            val chip = _binding.chipGroup.getChildAt(i) as Chip
            val zone = allZones[i]
            val isSelected = selectedFilters.contains(zone.zoneName)
            
            chip.isChecked = isSelected
            
            // Устанавливаем цвета в зависимости от состояния
            if (isSelected) {
                // Выбранный чип: синий фон, белый текст
                chip.setChipBackgroundColorResource(R.color.blue)
                chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                // Невыбранный чип: стандартный фон, стандартный текст
                chip.setChipBackgroundColorResource(R.color.chip_background)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text))
            }
        }
    }

    private fun initRcView() = with(_binding) {
        rcView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChooseExercisesAdapter(this@ChooseExercisesFragment)
        rcView.adapter = adapter
    }

    private fun exerciseListObserver() {
        model.exerciseListData.observe(viewLifecycleOwner) { exercises ->
            allExercises = exercises
            applyCurrentFilter()  // Применяем текущий фильтр
        }
    }
    
    private fun applyCurrentFilter() {
        Log.d("ChooseExercisesFragment", "applyCurrentFilter: selectedFilters = $selectedFilters, allExercises.size = ${allExercises.size}")
        
        val filteredExercises = if (selectedFilters.isEmpty()) {
            Log.d("ChooseExercisesFragment", "Показываем все упражнения: ${allExercises.size}")
            allExercises  // Показываем все
        } else {
            val filtered = allExercises.filter { exercise ->
                // Получаем зоны упражнения из базы (английские названия)
                val exerciseZones = exercise.muscleZone?.split(",")?.map { it.trim() } ?: emptyList()
                // Проверяем, соответствует ли упражнение хотя бы одному из выбранных фильтров
                exerciseZones.any { zone ->
                    selectedFilters.contains(zone)
                }
            }
            Log.d("ChooseExercisesFragment", "Отфильтровано упражнений: ${filtered.size} для фильтров: $selectedFilters")
            filtered
        }
        adapter.submitList(filteredExercises)
        updateChipStates()
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

}

