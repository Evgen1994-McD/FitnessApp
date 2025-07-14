package com.example.fitnessapp.customTraining.chooseExercises.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.chooseExercises.ui.adapter.ChooseExercisesAdapter
import com.example.fitnessapp.databinding.FragmentChooseExercisesBinding
import com.example.fitnessapp.databinding.FragmentSelectedExerciseListBinding

class ChooseExercisesFragment : Fragment(), ChooseExercisesAdapter.Listener {
    private lateinit var adapter: ChooseExercisesAdapter
    private var binding: FragmentChooseExercisesBinding? = null
    private val _binding get() = binding!!

    private val model: ChooseExercisesViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChooseExercisesBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }

    private fun initRcView() = with(_binding){
        rcView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChooseExercisesAdapter(this@ChooseExercisesFragment)
        rcView.adapter = adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(id: Int) {

    }

}