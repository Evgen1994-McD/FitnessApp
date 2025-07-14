package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.selectedExerciseList.adapter.SelectedListExerciseAdapter
import com.example.fitnessapp.databinding.FragmentSelectedExerciseListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedExerciseListFragment : Fragment() {
private var binding: FragmentSelectedExerciseListBinding? = null
    private val _binding get() = binding!!
    private lateinit var adapter: SelectedListExerciseAdapter

    private val model: SelectedExerciseListViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectedExerciseListBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }


    private fun initRcView(){
        _binding.apply {
            rcView.layoutManager = LinearLayoutManager(requireContext())
            adapter = SelectedListExerciseAdapter()
            rcView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
    }
}