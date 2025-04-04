package com.example.fitnessapp.training.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitnessapp.training.ui.fragments.DaysFragment
import com.example.fitnessapp.training.utils.TrainingUtils

class VpAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
      return DaysFragment.newInstance(TrainingUtils.difListType[position]) // здесь в нью инстанс нужно передать уровень сложности, ведь у нас пока там ничего нет, пофиксим ДайзФрагмент
    } // Чтобы передать сложность пришлось внести изменения в DaysFragment

    override fun getItemCount(): Int {
        return TrainingUtils.difListType.size  // А это обджект со сложностью который создали чуть ранее ( изи, мидл, хард)
            // по нему мы определим что показать в адаптере
    }
}