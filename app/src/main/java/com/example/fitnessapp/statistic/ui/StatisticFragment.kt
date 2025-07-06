package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSettingsBinding
import com.example.fitnessapp.databinding.FragmentStatisticBinding




class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null //ЭТО сам байндинг Налл
    private val binding get() = _binding!! // а здесь мы получаем байндинг



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    /*
    В onDestroyView наш байндинг приравниваем обратно к null
    Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
    но доступ к байдингу всё ещё есть
     */

}