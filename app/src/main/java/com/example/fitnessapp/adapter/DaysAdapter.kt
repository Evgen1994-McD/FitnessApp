package com.example.fitnessapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.DaysListItemBinding

class DaysAdapter : ListAdapter<DayModel, DaysAdapter.DayHolder > {

    class DayHolder(view : View) : RecyclerView.ViewHolder(view){  // это старый знакомый ViewHolder
        private val binding = DaysListItemBinding.bind(view)
        fun setData(day :DayModel) = with(binding) {  // прикольная фича. Чтобы не писать binding.tvName А писать сразу tvName. Круто! Напрямую

            val name = root.context.getString(R.string.day) +" "+ "${adapterPosition+1}"   // через контекст !binding! получили доступ к ресурсам и собрали строку из
            tvName.text = name
            val exCounter = day.exercises.split(",").size.toString() // мы передали сюда day. А там есть строка exercices. Мы сейчас её разделим по символу и получим массив. c
 tvCounter.text = exCounter // передали строку которую перевели в массив, узнали её размер и перевели в стринг. Таким образом мы узнали количетство упражнений в каждом дне
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder { // сюда передаётся вью.
val view = LayoutInflater.from(parent.context).inflate(R.layout.days_list_item, parent, false)  // Надули элемент вью. Урок 8 1 часть
return DayHolder(view)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) { // а здесь заполняем
        TODO("Not yet implemented")
    }

}