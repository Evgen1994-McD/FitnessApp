package com.example.fitnessapp.customTraining.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentCustomDaysListBinding
import com.example.fitnessapp.utils.DialogManager


class CustomDaysListFragment : Fragment() {

        private var _binding: FragmentCustomDaysListBinding? = null //ЭТО сам байндинг Налл
        private val binding get() = _binding!! // а здесь мы получаем байндинг


        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            _binding = FragmentCustomDaysListBinding.inflate(
                inflater,
                container,
                false
            )
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        /*
        В onDestroyView наш байндинг приравниваем обратно к null
        Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
        но доступ к байдингу всё ещё есть
         */

    }


