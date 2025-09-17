package com.example.fitnessapp.exercises.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.DaysListItemBinding
import com.example.fitnessapp.db.DayModel

class DaysAdapter(var listener: Listener) :
    ListAdapter<DayModel, DaysAdapter.DayHolder>(MyComporator()) { // А вот сюда мы запишем компоратор который отвечает за сравнение элеентов. А так же сюда передаем листенер Интерфейс

    class DayHolder(view: View) : RecyclerView.ViewHolder(view) {  // это старый знакомый ViewHolder
        private val binding = DaysListItemBinding.bind(view)


        fun setData(day: DayModel, listener: Listener) =
            with(binding) {  // прикольная фича. Чтобы не писать binding.tvName А писать сразу tvName. Круто! Напрямую

                val name =
                    root.context.getString(R.string.day) + " " + "${adapterPosition + 1}"   // через контекст !binding! получили доступ к ресурсам и собрали строку из
                tvName.text = name
                checkBoxImage.visibility = if (day.isDone) View.VISIBLE else View.INVISIBLE // заменили чек бокс и прописали условие
                lockImage.visibility = if (day.isOpen) View.INVISIBLE else View.VISIBLE

                val exCounter =
                    day.exercises.split(",").size.toString() // мы передали сюда day. А там есть строка exercices. Мы сейчас её разделим по символу и получим массив. c
                tvCounter.text = if (day.exercises.isEmpty()) {
                     "0 " + root.context.getString(R.string.exercises)
                } else {
                    exCounter + " " + root.context.getString(R.string.exercises)// передали строку которую перевели в массив, узнали её размер и перевели в стринг. Таким образом мы узнали количетство упражнений в каждом дне
                }
                itemView.setOnClickListener { listener.onClick(day.copy(dayNumber = adapterPosition + 1)) }

            }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DayHolder { // сюда передаётся вью.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.days_list_item, parent, false)  // Надули элемент вью. Урок 8 1 часть
        return DayHolder(view)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) { // а здесь заполняем
        holder.setData(getItem(position), listener)

    }

    class MyComporator :
        DiffUtil.ItemCallback<DayModel>() {  // Для того чтобы не заполнять одним и тем же элементом если он повторяется, мы делаем типа класс для сравнения. То есть если будет один и тот же элемент на одной позиции он не будет создаваться, а если разные то будет.
        override fun areItemsTheSame(
            oldItem: DayModel,
            newItem: DayModel
        ): Boolean { // это методы которые обязательно надо переопределить при использовании и наследовании от класса диффутил итем колбек
            return oldItem == newItem  // мы прописали логику для сравнения элементов.

        }

        override fun areContentsTheSame(oldItem: DayModel, newItem: DayModel): Boolean {

            return oldItem == newItem // мы прописали логику для сравнения элементов.
        }

    }

    interface Listener {   // Интерфейс для того чтобы переходить потом из DaysFragment в ExListFragment
        fun onClick(day: DayModel)
    }

}