package com.example.fitnessapp.customTraining.ui.chooseExercises

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

    class ExerciseHolder(view: View, val listener: Listener) :
        RecyclerView.ViewHolder(view) {  // это старый знакомый ViewHolder
        private val binding = ChooseExerciseItemBinding.bind(view)



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
            if (exercise.time.startsWith("x")) {
                // Упражнение с повторениями - показываем repsLayout
                binding.repsLayout.visibility = View.VISIBLE
                binding.timeLayout.visibility = View.GONE
                binding.etCount.visibility = View.GONE
                
                // Устанавливаем значение в EditText
                val count = exercise.time.substring(1) // Убираем "x"
                binding.etReps.setText(count)
                
            } else {
                // Упражнение с временем - показываем timeLayout
                binding.timeLayout.visibility = View.VISIBLE
                binding.repsLayout.visibility = View.GONE
                binding.etCount.visibility = View.GONE
                
                // Устанавливаем значения минут и секунд
                val timeSeconds = exercise.time.toLongOrNull() ?: 0L
                val minutes = (timeSeconds / 60).toInt()
                val seconds = (timeSeconds % 60).toInt()
                
                binding.etMinutes.setText(String.format("%02d", minutes))
                binding.etSeconds.setText(String.format("%02d", seconds))
            }
        }

        private fun setupFavoriteIcon(exercise: ExerciseModel) {
            binding.ivFavorite.setImageResource(
                if (exercise.isFavorite) R.drawable.ic_favorite_filled_24 
                else R.drawable.ic_favorite_border_24
            )
        }

        private fun setupCheckBox(exercise: ExerciseModel) {
            binding.checkboxSelect.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // Получаем измененное значение из EditText
                    val updatedTime = getUpdatedTimeFromEditText(exercise)
                    val updatedExercise = exercise.copy(time = updatedTime)
                    listener.onClick(updatedExercise)
                } else {
                    listener.onRemoveClick(exercise)
                }
            }
        }

        private fun getUpdatedTimeFromEditText(exercise: ExerciseModel): String {
            return if (exercise.time.startsWith("x")) {
                // Для упражнений с повторениями
                val repsText = binding.etReps.text.toString()
                val reps = repsText.filter { it.isDigit() }
                if (reps.isNotEmpty()) {
                    "x$reps"
                } else {
                    exercise.time // Возвращаем оригинальное значение если поле пустое
                }
            } else {
                // Для упражнений с временем
                try {
                    val minutesText = binding.etMinutes.text.toString()
                    val secondsText = binding.etSeconds.text.toString()
                    
                    val minutes = minutesText.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val seconds = secondsText.filter { it.isDigit() }.toIntOrNull() ?: 0
                    
                    val totalSeconds = minutes * 60 + seconds
                    if (totalSeconds > 0) totalSeconds.toString() else exercise.time
                } catch (e: Exception) {
                    exercise.time // Возвращаем оригинальное значение при ошибке
                }
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
        return ExerciseHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ExerciseHolder, position: Int) {
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