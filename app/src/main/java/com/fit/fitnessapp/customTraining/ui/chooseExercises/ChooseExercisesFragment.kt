package com.fit.fitnessapp.customTraining.ui.chooseExercises

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
import com.fit.fitnessapp.R
import com.fit.fitnessapp.databinding.FragmentChooseExercisesBinding
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.exercises.ui.compose.ExerciseBottomSheet
import com.fit.fitnessapp.ui.theme.FitnessAppTheme
import android.widget.Button
import android.widget.LinearLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseExercisesFragment : Fragment(), ChooseExercisesAdapter.Listener {
    private var newExercises = "" // Список ID выбранных упражнений (оставим для совместимости)
    private lateinit var adapter: ChooseExercisesAdapter
    private var binding: FragmentChooseExercisesBinding? = null
    private val _binding get() = binding as FragmentChooseExercisesBinding

    private val model: ChooseExercisesViewModel by viewModels()
    
    private var isBottomSheetShowing = false // Флаг для дебаунса
    private var allExercises = listOf<ExerciseModel>()
    private val selectedFilters = mutableSetOf<String>()  // Множество выбранных фильтров (английские названия для фильтрации)
    private var searchQuery = ""  // Текстовый поисковый запрос
    
    // Все зоны для фильтрации
    private val allZones = listOf(
        ZoneFilter("shoulders", "Плечи", 0, false),
        ZoneFilter("hands", "Руки", 0, false),
        ZoneFilter("chest", "Грудь", 0, false),
        ZoneFilter("back", "Спина", 0, false),
        ZoneFilter("abs", "Пресс", 0, false),
        ZoneFilter("legs", "Ноги", 0, false),
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
        setupSearchField()
        
        _binding.doneButton.setOnClickListener {
            // Получаем актуальные значения из EditText для всех выбранных упражнений
            saveExercisesWithCurrentValues()
        }
        getArgs()
        initRcView()
        exerciseListObserver()
        model.getAllExercises()
    }

    private fun setupSearchField() = with(_binding) {
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyCurrentFilter()
                // Показываем или скрываем крестик в зависимости от наличия текста
                updateClearButtonVisibility()
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Обрабатываем нажатие на крестик
        searchEditText.setOnTouchListener { view, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableEnd = 2 // Индекс drawableEnd
                if (searchEditText.compoundDrawables[drawableEnd] != null) {
                    val drawableWidth = searchEditText.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0
                    if (event.rawX >= (searchEditText.right - drawableWidth - searchEditText.paddingEnd)) {
                        // Нажали на крестик - очищаем текст
                        searchEditText.text?.clear()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
        
        // Инициально скрываем крестик
        updateClearButtonVisibility()
    }
    
    private fun updateClearButtonVisibility() {
        val hasText = _binding.searchEditText.text?.isNotEmpty() == true
        val drawable = if (hasText) {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear_text)
        } else {
            null
        }
        _binding.searchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, drawable, null
        )
    }

    private fun setupFilterButtons() {
        val filterButtonsRow1 = _binding.filterButtonsRow1
        val filterButtonsRow2 = _binding.filterButtonsRow2
        
        // Создаем кнопки для всех предопределенных зон
        filterButtonsRow1.removeAllViews()
        filterButtonsRow2.removeAllViews()
        
        // Распределяем кнопки по двум рядам (5 вверху, 3 внизу)
        val midPoint = 5
        
        allZones.forEachIndexed { index, zone ->
            val button = createFilterButton(zone.zoneName)
            if (index < midPoint) {
                filterButtonsRow1.addView(button)
            } else {
                filterButtonsRow2.addView(button)
            }
        }
        
        // Добавляем кнопку "Избранное" в нижний ряд справа как обычную кнопку
        val favoriteButton = createFilterButton("Избранное")
        filterButtonsRow2.addView(favoriteButton)
        
        updateButtonStates()
    }
    
    private fun createFilterButton(zone: String): Button {
        return Button(requireContext()).apply {
            text = if (zone == "Избранное") {
                "Избранное"
            } else {
                translateZoneName(zone)
            }
            textSize=11F
            // Устанавливаем начальные цвета
            val isSelected = selectedFilters.contains(zone)
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
                if (zone == "Избранное") {
                    LinearLayout.LayoutParams.WRAP_CONTENT // Для кнопки "Избранное" используем WRAP_CONTENT
                } else {
                    0 // width = 0dp для использования веса
                },
                LinearLayout.LayoutParams.WRAP_CONTENT,
                if (zone == "Избранное") {
                    0f // Для кнопки "Избранное" не используем вес
                } else {
                    1.0f // вес = 1 для равномерного распределения
                }
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            
            setOnClickListener {
                selectZone(zone)
            }
        }
    }
    
    private fun selectZone(zoneName: String) {
        Log.d("ChooseExercisesFragment", "selectZone: $zoneName, selectedFilters: $selectedFilters")
        
        if (zoneName == "Избранное") {
            // Особая обработка для фильтра "Избранное"
            if (selectedFilters.contains("Избранное")) {
                selectedFilters.remove("Избранное")
            } else {
                selectedFilters.add("Избранное")
            }
        } else {
            // Обработка обычных фильтров зон
            if (selectedFilters.contains(zoneName)) {
                selectedFilters.remove(zoneName)
            } else {
                selectedFilters.add(zoneName)
            }
        }
        
        applyCurrentFilter()
    }
    
    private fun updateButtonStates() {
        // Обновляем кнопки в первом ряду
        for (i in 0 until _binding.filterButtonsRow1.childCount) {
            val button = _binding.filterButtonsRow1.getChildAt(i) as Button
            if (i < allZones.size) {
                val zone = allZones[i]
                updateButtonAppearance(button, zone.zoneName)
            }
        }
        
        // Обновляем кнопки во втором ряду
        for (i in 0 until _binding.filterButtonsRow2.childCount) {
            val button = _binding.filterButtonsRow2.getChildAt(i) as Button
            
            // Проверяем, является ли это кнопка "Избранное" (последняя кнопка)
            if (i == _binding.filterButtonsRow2.childCount - 1) {
                // Это кнопка "Избранное"
                updateButtonAppearance(button, "Избранное")
            } else {
                // Это обычная кнопка зоны
                val zoneIndex = i + _binding.filterButtonsRow1.childCount
                if (zoneIndex < allZones.size) {
                    val zone = allZones[zoneIndex]
                    updateButtonAppearance(button, zone.zoneName)
                }
            }
        }
    }
    
    private fun updateButtonAppearance(button: Button, filterName: String) {
        val isSelected = selectedFilters.contains(filterName)
        
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
        Log.d("ChooseExercisesFragment", "applyCurrentFilter: selectedFilters = $selectedFilters, searchQuery = '$searchQuery', allExercises.size = ${allExercises.size}")
        
        var filteredExercises = allExercises
        
        // Применяем фильтрацию по зонам и избранному
        if (selectedFilters.isNotEmpty()) {
            filteredExercises = filteredExercises.filter { exercise ->
                val exerciseZones = exercise.muscleZone?.split(",")?.map { it.trim() } ?: emptyList()
                
                // Проверяем фильтр "Избранное"
                val isFavoriteSelected = selectedFilters.contains("Избранное")
                val isFavoriteMatch = if (isFavoriteSelected) {
                    exercise.isFavorite == true
                } else {
                    true // Если фильтр "Избранное" не выбран, игнорируем это условие
                }
                
                // Проверяем фильтры по зонам (только те, что не "Избранное")
                val zoneFilters = selectedFilters.filter { it != "Избранное" }
                val isZonesMatch = if (zoneFilters.isNotEmpty()) {
                    // Упражнение должно содержать ХОТЯ БЫ ОДНУ из выбранных зон (ИЛИ)
                    zoneFilters.any { zone ->
                        exerciseZones.contains(zone)
                    }
                } else {
                    true // Если фильтры по зонам не выбраны, игнорируем это условие
                }
                
                // Упражнение должно соответствовать всем выбранным фильтрам
                isFavoriteMatch && isZonesMatch
            }
            Log.d("ChooseExercisesFragment", "После фильтрации по зонам/избранному: ${filteredExercises.size} упражнений")
        }
        
        // Применяем поиск по названию
        if (searchQuery.isNotBlank()) {
            val searchLower = searchQuery.lowercase()
            filteredExercises = filteredExercises.filter { exercise ->
                exercise.name.lowercase().contains(searchLower)
            }
            Log.d("ChooseExercisesFragment", "После поиска по названию: ${filteredExercises.size} упражнений")
        }
        
        Log.d("ChooseExercisesFragment", "Итоговый результат: ${filteredExercises.size} упражнений")
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
        exercise.id?.let { id ->
            if (id != -1) {
                adapter.selectExercise(id)
                updateCounter()
            }
        }
    }

    override fun onRemoveClick(exercise: ExerciseModel) {
        exercise.id?.let { id ->
            if (id != -1) {
                adapter.deselectExercise(id)
                updateCounter()
            }
        }
    }

    private fun updateCounter() {
        val count = adapter.getSelectedExercises().size
        val choosenCounterText = "${getString(R.string.selected_exercise_count)} $count"
        _binding.tvChoosenExCounter.text = choosenCounterText
    }

    private fun saveExercisesWithCurrentValues() {
        val selectedExerciseIds = adapter.getSelectedExercises()
        android.util.Log.d("ChooseExercisesFragment", "Сохранение упражнений. Выбранные ID: $selectedExerciseIds")
        
        if (selectedExerciseIds.isEmpty()) {
            android.util.Log.d("ChooseExercisesFragment", "Нет выбранных упражнений для сохранения")
            model.updateDay("")
            findNavController().popBackStack()
            return
        }
        
        val finalExerciseIds = mutableListOf<String>()
        var processedCount = 0
        
        // Проходим по всем выбранным упражнениям
        selectedExerciseIds.forEach { exerciseId ->
            val exercise = adapter.currentList.find { it.id == exerciseId }
            android.util.Log.d("ChooseExercisesFragment", "Обработка упражнения ID: $exerciseId, найдено: ${exercise?.name}")
            
            if (exercise != null) {
                // Получаем упражнение с измененными значениями из адаптера
                val updatedExercise = adapter.getExerciseWithChanges(exerciseId, exercise)
                android.util.Log.d("ChooseExercisesFragment", "Упражнение ${exercise.name}, оригинальное время: ${exercise.time}, измененное время: ${updatedExercise.time}")
                
                // Всегда создаем новое упражнение, чтобы можно было добавлять одно и то же упражнение несколько раз
                val newExercise = exercise.copy(
                    id = null, // Обнуляем ID чтобы создать новую запись
                    time = updatedExercise.time
                )
                android.util.Log.d("ChooseExercisesFragment", "Создание нового упражнения: ${newExercise.name}, время: ${newExercise.time}")
                
                // Сохраняем новое упражнение в базу данных
                model.insertCustomExercise(newExercise) { newId ->
                    android.util.Log.d("ChooseExercisesFragment", "Новое упражнение сохранено с ID: $newId")
                    synchronized(finalExerciseIds) {
                        if (newId != -1L) {
                            finalExerciseIds.add(newId.toString())
                            android.util.Log.d("ChooseExercisesFragment", "Добавлен ID в список. Текущий список: $finalExerciseIds")
                        }
                    }
                    processedCount++
                    checkAndComplete(processedCount, selectedExerciseIds.size, finalExerciseIds)
                }
            } else {
                android.util.Log.d("ChooseExercisesFragment", "Упражнение с ID $exerciseId не найдено в списке")
                processedCount++
                checkAndComplete(processedCount, selectedExerciseIds.size, finalExerciseIds)
            }
        }
    }
    
    private fun checkAndComplete(processedCount: Int, totalCount: Int, finalExerciseIds: MutableList<String>) {
        // Проверяем все ли упражнения обработаны
        if (processedCount == totalCount) {
            synchronized(finalExerciseIds) {
                val exercisesString = finalExerciseIds.joinToString(",")
                android.util.Log.d("ChooseExercisesFragment", "Все упражнения обработаны. Итоговые ID: $finalExerciseIds")
                android.util.Log.d("ChooseExercisesFragment", "Строка для сохранения: $exercisesString")
                model.updateDay(exercisesString)
                findNavController().popBackStack()
            }
        }
    }

    override fun onLongClick(exercise: ExerciseModel) {
        showExerciseBottomSheet(exercise)
    }

    override fun onInfoClick(exercise: ExerciseModel) {
        showExerciseBottomSheet(exercise)
    }

    override fun onFavoriteClick(exercise: ExerciseModel) {
        Log.d("ChooseExercisesFragment", "onFavoriteClick: ${exercise.name}, текущий isFavorite: ${exercise.isFavorite}")
        
        // Переключаем статус избранного
        val updatedExercise = exercise.copy(isFavorite = !exercise.isFavorite)
        
        Log.d("ChooseExercisesFragment", "Новый статус: ${updatedExercise.isFavorite}")
        
        // Обновляем в базе данных
        model.updateExerciseFavorite(updatedExercise)
        
        // Обновляем в списке упражнений
        val currentList = allExercises.toMutableList()
        val index = currentList.indexOfFirst { it.id == exercise.id }
        if (index != -1) {
            currentList[index] = updatedExercise
            allExercises = currentList
            
            Log.d("ChooseExercisesFragment", "Обновлено в allExercises: индекс $index")
            
            // Применяем фильтры заново чтобы обновить отображение
            applyCurrentFilter()
            
            // Дополнительно обновляем конкретный элемент в адаптере для немедленного обновления иконки
            val currentFilteredIndex = adapter.currentList.indexOfFirst { it.id == exercise.id }
            if (currentFilteredIndex != -1) {
                adapter.notifyItemChanged(currentFilteredIndex)
            }
            
            // Показываем сообщение
            val message = if (updatedExercise.isFavorite) {
                "Добавлено в избранное: ${exercise.name}"
            } else {
                "Удалено из избранного: ${exercise.name}"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } else {
            Log.e("ChooseExercisesFragment", "Упражнение не найдено в allExercises: ${exercise.id}")
        }
        
        Log.d("ChooseExercisesFragment", "onFavoriteClick завершен")
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

