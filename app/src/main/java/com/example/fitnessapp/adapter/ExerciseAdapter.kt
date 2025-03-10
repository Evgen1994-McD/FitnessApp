package com.example.fitnessapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.DaysListItemBinding
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.databinding.ExerciseListItemBinding

// Мы скопировали DaysAdapter и переделали его чтобы не писать заново
class ExerciseAdapter() : ListAdapter<ExerciseModel, ExerciseAdapter.ExerciseHolder>(MyComporator()) { // А вот сюда мы запишем компоратор который отвечает за сравнение элеентов. А так же сюда передаем листенер Интерфейс

    class ExerciseHolder(view : View) : RecyclerView.ViewHolder(view){  // это старый знакомый ViewHolder
        private val binding = ExerciseListItemBinding.bind(view)

        fun setData(exercise : ExerciseModel) = with(binding) {

            tvNameEx.text = exercise.name
            tvcount.text = exercise.time

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseHolder {
val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_list_item, parent, false)
return ExerciseHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseHolder, position: Int) {
  holder.setData(getItem(position))

    }

    class MyComporator : DiffUtil.ItemCallback<ExerciseModel>(){
        override fun areItemsTheSame(oldItem: ExerciseModel, newItem: ExerciseModel): Boolean {
return oldItem == newItem

        }

        override fun areContentsTheSame(oldItem: ExerciseModel, newItem: ExerciseModel): Boolean {

            return oldItem == newItem
        }

    }


}