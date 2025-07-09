package com.example.fitnessapp.statistic.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.DateSelectedListItemBinding
import com.example.fitnessapp.statistic.adapters.DateSelectorAdapter.Holder
import com.example.fitnessapp.statistic.data.DateSelectorModel

class DateSelectorAdapter(private val listener: Listener): ListAdapter<DateSelectorModel, Holder>(Comparator()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): Holder {
    val view = LayoutInflater.from(parent.context).inflate(
        R.layout.date_selected_list_item,
        parent,
        false
    )
        return Holder(view, listener)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int,
    ) {
  holder.bind(getItem(position))
    }

    class Holder(view: View, private val listener: Listener): RecyclerView.ViewHolder(view) {
private val binding = DateSelectedListItemBinding.bind(view)

        fun bind(dateSelectorModel: DateSelectorModel) = with(binding){
           item.text = dateSelectorModel.text
            if (item.isSelected){
                item.setTextColor(Color.WHITE)
                item.setBackgroundResource(R.drawable.date_selected_bg)
            } else {
                item.setTextColor(Color.GRAY)
                item.setBackgroundResource(R.drawable.date_unselected_bg)
            }
            item.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }

        }

    }

    class Comparator : DiffUtil.ItemCallback<DateSelectorModel>(){
        override fun areItemsTheSame(
            oldItem: DateSelectorModel,
            newItem: DateSelectorModel,
        ): Boolean {
           return oldItem.text == oldItem.text
        }

        override fun areContentsTheSame(
            oldItem: DateSelectorModel,
            newItem: DateSelectorModel,
        ): Boolean {
return oldItem == newItem
        }

    }

    interface Listener{
        fun onItemClick(index: Int)
    }

}