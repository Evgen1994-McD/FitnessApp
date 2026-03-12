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
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setMargins
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
        
        // Настройка кнопок для фильтрации
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
        // Разделяем зоны на два ряда
        val firstRowZones = allZones.take(5) // Первые 5 зон
        val secondRowZones = allZones.drop(5) // Остальные зоны
        
        // Создаем кнопки для первого ряда
        firstRowZones.forEach { zone ->
            val button = createFilterButton(zone)
            filterButtonsRow1.addView(button)
        }
        
        // Создаем кнопки для второго ряда
        secondRowZones.forEach { zone ->
            val button = createFilterButton(zone)
            filterButtonsRow2.addView(button)
        }
    }
    
    private fun createFilterButton(zone: ZoneFilter): Button {
        return Button(requireContext()).apply {
            text = zone.displayName  // Показываем русское название
            textSize=11F
            // Устанавливаем начальные цвета
            val isSelected = selectedFilters.contains(zone.zoneName)
            if (isSelected) {
                // Выбранная кнопка: синий фон, белый текст
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                // Невыбранная кнопка: стандартный фон, стандартный текст
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chip_background))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text))
            }
            
            // Устанавливаем отступы
            setPadding(24, 12, 24, 12)
            // Устанавливаем минимальную высоту
            minHeight = 48
            
            // Устанавливаем вес для равномерного распределения
            layoutParams = LinearLayout.LayoutParams(
                0, // width = 0dp для использования веса
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f // вес = 1 для равномерного распределения
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            
            setOnClickListener {
                selectZone(zone)
            }
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
    
    private fun updateButtonStates() {
        // Обновляем кнопки в первом ряду
        for (i in 0 until _binding.filterButtonsRow1.childCount) {
            val button = _binding.filterButtonsRow1.getChildAt(i) as Button
            val zone = allZones[i]
            updateButtonAppearance(button, zone)
        }
        
        // Обновляем кнопки во втором ряду
        for (i in 0 until _binding.filterButtonsRow2.childCount) {
            val button = _binding.filterButtonsRow2.getChildAt(i) as Button
            val zone = allZones[i + 5] // Смещение на 5 для второго ряда
            updateButtonAppearance(button, zone)
        }
    }
    
    private fun updateButtonAppearance(button: Button, zone: ZoneFilter) {
        val isSelected = selectedFilters.contains(zone.zoneName)
        
        // Устанавливаем цвета в зависимости от состояния
        if (isSelected) {
            // Выбранная кнопка: синий фон, белый текст
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
            button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            // Невыбранная кнопка: стандартный фон, стандартный текст
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chip_background))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text))
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
        updateButtonStates()
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

