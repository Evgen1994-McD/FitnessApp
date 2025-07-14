package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.selectedExerciseList.adapter.SelectedListExerciseAdapter
import com.example.fitnessapp.databinding.FragmentSelectedExerciseListBinding
import com.example.fitnessapp.db.ExerciseModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedExerciseListFragment : Fragment(), SelectedListExerciseAdapter.Listener {
    val exerciseList = listOf(
        ExerciseModel(
            null,
            "Push up",
            "",
            "x30",
            false,
            "otszhimania.gif",
            62

        ),
        ExerciseModel(
            null,
            "Bicycle",
            "",
            "30",
            false,
            "bicycle.gif",
            62

        ),
        ExerciseModel(
            null,
            "Push up",
            "",
            "x50",
            false,
            "otszhimania.gif",
            62

        )
    )
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
            adapter = SelectedListExerciseAdapter(this@SelectedExerciseListFragment)
            rcView.adapter = adapter
            adapter.submitList(exerciseList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
    }

    override fun onDelete() {
    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
    }
}