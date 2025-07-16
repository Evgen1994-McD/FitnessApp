package com.example.fitnessapp.customTraining.chooseExercises.ui.adapter

import android.animation.Animator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ExerciseListItemBinding
import com.example.fitnessapp.databinding.SelectedExerciseListItemBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.utils.TimeUtils
import pl.droidsonroids.gif.GifDrawable

// Мы скопировали DaysAdapter и переделали его чтобы не писать заново
class ChooseExercisesAdapter(val listener: Listener) :
    ListAdapter<ExerciseModel, ChooseExercisesAdapter.ExerciseHolder>(MyComporator()) { // А вот сюда мы запишем компоратор который отвечает за сравнение элеентов. А так же сюда передаем листенер Интерфейс

    class ExerciseHolder(view: View, val listener: Listener) :
        RecyclerView.ViewHolder(view) {  // это старый знакомый ViewHolder
        private val binding = SelectedExerciseListItemBinding.bind(view)

        init {
            binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    binding.lottieView.visibility = View.INVISIBLE

                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
        }

        fun setData(exercise: ExerciseModel) = with(binding) {

delete.visibility = View.INVISIBLE
            tvNameEx.text = exercise.name //Название упражнения
            tvcount.text =
                getTime(exercise.time)
            imExercise.setImageDrawable(
                GifDrawable(
                    root.context.assets,
                    exercise.image
                )
            ) // Покажем ГИФ с помощью специальной библиотеки
            itemView.setOnClickListener {
                listener.onClick(exercise)
                binding.lottieView.visibility = View.VISIBLE

                lottieView.playAnimation()
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
            LayoutInflater.from(parent.context).inflate(R.layout.selected_exercise_list_item, parent, false)
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
    }
}