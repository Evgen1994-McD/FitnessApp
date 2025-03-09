package com.example.fitnessapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.DaysListItemBinding

class DaysAdapter : ListAdapter<DayModel, DaysAdapter.DayHolder>(MyComporator()) { // А вот сюда мы запишем компоратор который отвечает за сравнение элеентов

    class DayHolder(view : View) : RecyclerView.ViewHolder(view){  // это старый знакомый ViewHolder
        private val binding = DaysListItemBinding.bind(view)
        fun setData(day :DayModel) = with(binding) {  // прикольная фича. Чтобы не писать binding.tvName А писать сразу tvName. Круто! Напрямую

            val name = root.context.getString(R.string.day) +" "+ "${adapterPosition+1}"   // через контекст !binding! получили доступ к ресурсам и собрали строку из
            tvName.text = name
            val exCounter = day.exercises.split(",").size.toString() // мы передали сюда day. А там есть строка exercices. Мы сейчас её разделим по символу и получим массив. c
 tvCounter.text = exCounter+ " "+ root.context.getString(R.string.exercises)// передали строку которую перевели в массив, узнали её размер и перевели в стринг. Таким образом мы узнали количетство упражнений в каждом дне
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder { // сюда передаётся вью.
val view = LayoutInflater.from(parent.context).inflate(R.layout.days_list_item, parent, false)  // Надули элемент вью. Урок 8 1 часть
return DayHolder(view)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) { // а здесь заполняем
  holder.setData(getItem(position))

    }

    class MyComporator : DiffUtil.ItemCallback<DayModel>(){  // Для того чтобы не заполнять одним и тем же элементом если он повторяется, мы делаем типа класс для сравнения. То есть если будет один и тот же элемент на одной позиции он не будет создаваться, а если разные то будет.
        override fun areItemsTheSame(oldItem: DayModel, newItem: DayModel): Boolean { // это методы которые обязательно надо переопределить при использовании и наследовании от класса диффутил итем колбек
return oldItem == newItem  // мы прописали логику для сравнения элементов.

        }

        override fun areContentsTheSame(oldItem: DayModel, newItem: DayModel): Boolean {

            return oldItem == newItem // мы прописали логику для сравнения элементов.
        }

    }

}