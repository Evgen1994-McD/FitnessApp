package com.example.fitnessapp.statistic.adapters

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

class DateSelectorAdapter: ListAdapter<DateSelectorModel, Holder>(Comparator()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): Holder {
    val view = LayoutInflater.from(parent.context).inflate(
        R.layout.date_selected_list_item,
        parent,
        false
    )
        return Holder(view)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int,
    ) {
  holder.bind(getItem(position))
    }

    class Holder(view: View ): RecyclerView.ViewHolder(view) {
private val binding = DateSelectedListItemBinding.bind(view)

        fun bind(dateSelectorModel: DateSelectorModel) = with(binding){
           item.text = dateSelectorModel.text
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
}