package com.example.fitnessapp.training.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.fitnessapp.databinding.FragmentTrainingBinding
import com.example.fitnessapp.training.ui.adapters.VpAdapter
import com.example.fitnessapp.training.utilsfortraining.TrainingUtils
import com.google.android.material.tabs.TabLayoutMediator

class TrainingFragment : Fragment() {
private lateinit var binding: FragmentTrainingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTrainingBinding.inflate(inflater, container, false) //ТУТ ХЗ что, точно не понял
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vpAdapter = VpAdapter(this) // Мутим метод онВьюкреатед и вызываем тут наш адаптер ВПадаптер
        binding.vp.adapter = vpAdapter //привязываем адаптер к ВьюПейджеру. Но мы хотим связать Пейджер и ТабЛайоут
        //Для связки нужен специальный Медиатор
        TabLayoutMediator(binding.tabLayout, binding.vp){ tab, pos ->  // Для применения указать таблайоут + ВьюПейджер
//мы хотим менять названия у наших табов, сейчас этим и займемся
            tab.text = getString(TrainingUtils.tabTitles[pos]) //Передаём в табы текст из ресурсов по позиции
        }.attach() // обязательно делается Аттач, иначе не будет работать
    binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ // хотим знать какой фрагмент во вью пейджере
        //мы открыли
        override fun onPageSelected(position: Int) { // выбрали что будем знать фрагмент. тут передаётся 0, 1 или 2 позиция
            // в соответствие с позицией уже открывается изи мидл или хард
            super.onPageSelected(position)
        }
    })
    }


    }