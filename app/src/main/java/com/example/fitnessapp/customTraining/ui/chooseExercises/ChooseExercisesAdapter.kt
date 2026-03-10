package com.example.fitnessapp.customTraining.ui.chooseExercises

import android.animation.Animator
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

        init {
            binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    binding.lottieView.visibility = View.INVISIBLE
                    binding.infoIcon.visibility = View.VISIBLE // Показываем иконку ? снова
                }

                override fun onAnimationCancel(animation: Animator) {
                    binding.lottieView.visibility = View.INVISIBLE
                    binding.infoIcon.visibility = View.VISIBLE // Показываем иконку ? снова
                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
        }

        fun setData(exercise: ExerciseModel) = with(binding) {

            tvNameEx.text = exercise.name //Название упражнения
            tvCount.text = getTime(exercise.time)
            
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
            
            // Изначально показываем иконку ?, скрываем анимацию
            infoIcon.visibility = View.VISIBLE
            lottieView.visibility = View.INVISIBLE
            
            // Клик на иконку ? - показываем информацию
            infoIcon.setOnClickListener {
                listener.onInfoClick(exercise)
            }
            
            // Клик на элемент целиком (не на ?) - добавляем в тренировку и показываем анимацию
            itemView.setOnClickListener {
                listener.onClick(exercise)
                // Показываем анимацию поверх иконки ?
                infoIcon.visibility = View.INVISIBLE
                lottieView.visibility = View.VISIBLE
                lottieView.playAnimation()
            }
            
            itemView.setOnLongClickListener {
                listener.onLongClick(exercise)
                true // Возвращаем true, чтобы показать, что событие обработано
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
    }
}