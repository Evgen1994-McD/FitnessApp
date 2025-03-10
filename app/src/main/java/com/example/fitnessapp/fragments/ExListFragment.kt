package com.example.fitnessapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DayModel
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.adapter.ExerciseAdapter
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.databinding.FragmentDaysBinding
import com.example.fitnessapp.utils.MainViewModel
import java.util.zip.Inflater

class ExListFragment : Fragment() {
    private lateinit var binding: ExerciseListFragmentBinding
    private lateinit var adapter: ExerciseAdapter
    private val model: MainViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExerciseListFragmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        init()
model.mutableListExercise.observe(viewLifecycleOwner){  // Тут мы получаем список который создали ранее, посредси
adapter.submitList(it)
}
    }


private fun init() = with(binding) {  // Инициализируем Адаптер и добавляем RecyclerVIew
    adapter = ExerciseAdapter()
    rcView.adapter = adapter // Назначили адаптер

}

    companion object {

        @JvmStatic
        fun newInstance() = ExListFragment()


    }
}