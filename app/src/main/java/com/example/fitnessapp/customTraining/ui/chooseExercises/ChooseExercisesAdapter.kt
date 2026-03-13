package com.example.fitnessapp.customTraining.ui.chooseExercises

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ChooseExerciseItemBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.utils.TimeUtils
import com.example.fitnessapp.utils.ZoneUtils
import pl.droidsonroids.gif.GifDrawable

// Мы скопировали DaysAdapter и переделали его чтобы не писать заново
class ChooseExercisesAdapter(val listener: Listener) :
    ListAdapter<ExerciseModel, ChooseExercisesAdapter.ExerciseHolder>(MyComporator()) { // А вот сюда мы запишем компоратор который отвечает за сравнение элеентов. А так же сюда передаем листенер Интерфейс

    // Список выбранных упражнений по их ID
    private val selectedExerciseIds = mutableSetOf<Int>()
    
    // Карта для хранения измененных значений времени/количества
    private val exerciseTimeChanges = mutableMapOf<Int, String>()
    
    // Методы для управления выбранными упражнениями
    fun selectExercise(exerciseId: Int) {
        selectedExerciseIds.add(exerciseId)
    }
    
    fun deselectExercise(exerciseId: Int) {
        selectedExerciseIds.remove(exerciseId)
    }
    
    fun isExerciseSelected(exerciseId: Int): Boolean {
        return selectedExerciseIds.contains(exerciseId)
    }
    
    fun getSelectedExercises(): List<Int> {
        return selectedExerciseIds.toList()
    }
    
    // Методы для управления измененными значениями
    fun updateExerciseTime(exerciseId: Int, newTime: String) {
        exerciseTimeChanges[exerciseId] = newTime
    }
    
    fun getExerciseTime(exerciseId: Int): String? {
        return exerciseTimeChanges[exerciseId]
    }
    
    fun getExerciseWithChanges(exerciseId: Int, originalExercise: ExerciseModel): ExerciseModel {
        val changedTime = exerciseTimeChanges[exerciseId]
        return if (changedTime != null) {
            originalExercise.copy(time = changedTime)
        } else {
            originalExercise
        }
    }

    class ExerciseHolder(view: View, val adapter: ChooseExercisesAdapter, val listener: Listener) :
        RecyclerView.ViewHolder(view) {  // это старый знакомый ViewHolder
        private val binding = ChooseExerciseItemBinding.bind(view)
        
        // Сохраняем ссылки на TextWatcher чтобы их можно было удалить
        private var repsTextWatcher: TextWatcher? = null
        private var minutesTextWatcher: TextWatcher? = null
        private var secondsTextWatcher: TextWatcher? = null
        private var currentExerciseId: Int? = null

        fun clearOldData() {
            // Удаляем старые TextWatcher'ы
            repsTextWatcher?.let { binding.etReps.removeTextChangedListener(it) }
            minutesTextWatcher?.let { binding.etMinutes.removeTextChangedListener(it) }
            secondsTextWatcher?.let { binding.etSeconds.removeTextChangedListener(it) }
            
            // Очищаем ссылки
            repsTextWatcher = null
            minutesTextWatcher = null
            secondsTextWatcher = null
            currentExerciseId = null
        }

        fun setData(exercise: ExerciseModel) = with(binding) {

            tvNameEx.text = exercise.name //Название упражнения
            
            // Настраиваем EditText в зависимости от типа упражнения
            setupEditText(exercise)
            
            // Показываем зоны на русском
            val zonesDisplay = ZoneUtils.getZonesDisplayNames(exercise.muscleZone)
            android.util.Log.d("ChooseExercisesAdapter", "Упражнение: ${exercise.name}, зоны: ${exercise.muscleZone} -> $zonesDisplay")
            tvZones.text = zonesDisplay
            
            imExercise.setImageDrawable(
                GifDrawable(
                    root.context.assets,
                    exercise.image
                )
            ) // Покажем ГИФ с помощью специальной библиотеки
            
            // Настраиваем иконку избранного
            setupFavoriteIcon(exercise)
            
            // Настраиваем CheckBox
            setupCheckBox(exercise)
            
            // Клик на иконку ? - показываем информацию
            infoIcon.setOnClickListener {
                listener.onInfoClick(exercise)
            }
            
            // Клик на иконку избранного
            ivFavorite.setOnClickListener {
                listener.onFavoriteClick(exercise)
            }

        }

        private fun setupEditText(exercise: ExerciseModel) {
            exercise.id?.let { id ->
                // Сначала удаляем старые TextWatcher'ы
                repsTextWatcher?.let { binding.etReps.removeTextChangedListener(it) }
                minutesTextWatcher?.let { binding.etMinutes.removeTextChangedListener(it) }
                secondsTextWatcher?.let { binding.etSeconds.removeTextChangedListener(it) }
                
                if (exercise.time.startsWith("x")) {
                    // Упражнение с повторениями - показываем repsLayout
                    binding.repsLayout.visibility = View.VISIBLE
                    binding.timeLayout.visibility = View.GONE
                    binding.etCount.visibility = View.GONE
                    
                    // Получаем сохраненное значение или используем оригинальное
                    val savedTime = adapter.getExerciseTime(id)
                    val count = if (savedTime != null && savedTime.startsWith("x")) {
                        savedTime.substring(1) // Убираем "x"
                    } else {
                        exercise.time.substring(1) // Убираем "x" из оригинала
                    }
                    binding.etReps.setText(count)
                    
                    // Создаем и добавляем новый TextWatcher
                    repsTextWatcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val repsText = s?.toString() ?: ""
                            val reps = repsText.filter { it.isDigit() }
                            if (reps.isNotEmpty()) {
                                adapter.updateExerciseTime(id, "x$reps")
                            }
                        }
                    }
                    binding.etReps.addTextChangedListener(repsTextWatcher)
                    
                } else {
                    // Упражнение с временем - показываем timeLayout
                    binding.timeLayout.visibility = View.VISIBLE
                    binding.repsLayout.visibility = View.GONE
                    binding.etCount.visibility = View.GONE
                    
                    // Получаем сохраненное значение или используем оригинальное
                    val savedTime = adapter.getExerciseTime(id)
                    val timeSeconds = if (savedTime != null) {
                        savedTime.toLongOrNull() ?: 0L
                    } else {
                        exercise.time.toLongOrNull() ?: 0L
                    }
                    val minutes = (timeSeconds / 60).toInt()
                    val seconds = (timeSeconds % 60).toInt()
                    
                    binding.etMinutes.setText(String.format("%02d", minutes))
                    binding.etSeconds.setText(String.format("%02d", seconds))
                    
                    // Создаем отдельные TextWatcher для минут и секунд
                    minutesTextWatcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            updateTotalTime(id)
                        }
                    }
                    
                    secondsTextWatcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            updateTotalTime(id)
                        }
                    }
                    
                    binding.etMinutes.addTextChangedListener(minutesTextWatcher)
                    binding.etSeconds.addTextChangedListener(secondsTextWatcher)
                }
            }
        }

        private fun setupFavoriteIcon(exercise: ExerciseModel) {
            binding.ivFavorite.setImageResource(
                if (exercise.isFavorite) R.drawable.ic_favorite_filled_24 
                else R.drawable.ic_favorite_border_24
            )
        }

        private fun setupCheckBox(exercise: ExerciseModel) {
            // Сначала убираем listener чтобы избежать срабатывания при установке состояния
            binding.checkboxSelect.setOnCheckedChangeListener(null)
            
            // Устанавливаем состояние CheckBox на основе выбранности упражнения
            binding.checkboxSelect.isChecked = adapter.isExerciseSelected(exercise.id ?: -1)
            
            // Устанавливаем listener после установки состояния
            binding.checkboxSelect.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    listener.onClick(exercise)
                } else {
                    listener.onRemoveClick(exercise)
                }
            }
        }

        // Новый метод для получения актуальных значений из EditText
        fun getCurrentExerciseValues(exercise: ExerciseModel): ExerciseModel {
            return if (exercise.time.startsWith("x")) {
                // Для упражнений с повторениями
                val repsText = binding.etReps.text.toString()
                val reps = repsText.filter { it.isDigit() }
                if (reps.isNotEmpty()) {
                    exercise.copy(time = "x$reps")
                } else {
                    exercise
                }
            } else {
                // Для упражнений с временем
                try {
                    val minutesText = binding.etMinutes.text.toString()
                    val secondsText = binding.etSeconds.text.toString()
                    
                    val minutes = minutesText.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val seconds = secondsText.filter { it.isDigit() }.toIntOrNull() ?: 0
                    
                    val totalSeconds = minutes * 60 + seconds
                    if (totalSeconds > 0) {
                        exercise.copy(time = totalSeconds.toString())
                    } else {
                        exercise
                    }
                } catch (e: Exception) {
                    exercise
                }
            }
        }

        private fun updateTotalTime(exerciseId: Int) {
            val minutesText = binding.etMinutes.text.toString()
            val secondsText = binding.etSeconds.text.toString()
            
            val minutes = minutesText.filter { it.isDigit() }.toIntOrNull() ?: 0
            val seconds = secondsText.filter { it.isDigit() }.toIntOrNull() ?: 0
            
            val totalSeconds = minutes * 60 + seconds
            if (totalSeconds > 0) {
                adapter.updateExerciseTime(exerciseId, totalSeconds.toString())
            }
        }

        private fun getTime(time: String): String {
            return if (time.startsWith("x")) {
                time
            } else {
                TimeUtils.getTime(time.toLong() * 1000) // время или количество раз
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.choose_exercise_item, parent, false)
        return ExerciseHolder(view, this, listener)
    }

    override fun onBindViewHolder(holder: ExerciseHolder, position: Int) {
        // Очищаем старые данные перед привязкой новых
        holder.clearOldData()
        holder.setData(getItem(position))
    }

    class MyComporator : DiffUtil.ItemCallback<ExerciseModel>() {
        override fun areItemsTheSame(oldItem: ExerciseModel, newItem: ExerciseModel): Boolean {
            return oldItem == newItem

        }

        override fun areContentsTheSame(oldItem: ExerciseModel, newItem: ExerciseModel): Boolean {

            return oldItem == newItem
        }



    }

    interface Listener{
        fun onClick(exercise: ExerciseModel)
        fun onLongClick(exercise: ExerciseModel)
        fun onInfoClick(exercise: ExerciseModel)
        fun onFavoriteClick(exercise: ExerciseModel)
        fun onRemoveClick(exercise: ExerciseModel)
    }
}