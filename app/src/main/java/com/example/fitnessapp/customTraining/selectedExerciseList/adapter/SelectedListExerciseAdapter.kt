package com.example.fitnessapp.customTraining.selectedExerciseList.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.SelectedExerciseListItemBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pl.droidsonroids.gif.GifDrawable

// Мы скопировали DaysAdapter и переделали его чтобы не писать заново
class SelectedListExerciseAdapter( val listener: Listener) :
    ListAdapter<ExerciseModel, SelectedListExerciseAdapter.ExerciseHolder>(MyComporator()) { // А вот сюда мы запишем компоратор который отвечает за сравнение элеентов. А так же сюда передаем листенер Интерфейс

    class ExerciseHolder(view: View, val listener: Listener) :
        RecyclerView.ViewHolder(view) {  // это старый знакомый ViewHolder
        private val binding = SelectedExerciseListItemBinding.bind(view)

        fun setData(exercise: ExerciseModel) = with(binding) {


            tvNameEx.text = exercise.name //Название упражнения
            tvcount.text =
                getTime(exercise.time)
            imExercise.setImageDrawable(
                GifDrawable(
                    root.context.assets,
                    exercise.image
                )
            ) // Покажем ГИФ с помощью специальной библиотеки
delete.setOnClickListener {

    listener.onDelete(adapterPosition)
}
            up.setOnClickListener{

                    listener.addExerciseTime(adapterPosition)

            }
            down.setOnClickListener {

                    listener.decreaseExerciseTime(adapterPosition)

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
            return oldItem == newItem && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExerciseModel, newItem: ExerciseModel): Boolean {

            return oldItem.time == newItem.time && oldItem.id == newItem.id

        }



    }

    interface Listener{
        fun onDelete(pos: Int)
       fun addExerciseTime(pos:Int)
        fun decreaseExerciseTime(pos:Int)
    }
}