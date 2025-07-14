package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.selectedExerciseList.adapter.SelectedListExerciseAdapter
import com.example.fitnessapp.databinding.FragmentSelectedExerciseListBinding
import com.example.fitnessapp.db.ExerciseModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections

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
        savedInstanceState: Bundle?,
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
        _binding.addExercises.setOnClickListener {
            findNavController().navigate(R.id.chooseExercisesFragment)
        }
    }


    private fun initRcView() {
        _binding.apply {
            rcView.layoutManager = LinearLayoutManager(requireContext())
            adapter = SelectedListExerciseAdapter(this@SelectedExerciseListFragment)
            rcView.adapter = adapter
            adapter.submitList(exerciseList)
            createItemTouchHelper().attachToRecyclerView(rcView)
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) { /*
        с помощью ItemTouchHelper можем как перетаскивать вверх - вниз, так и свайпать элементы
        В данном случае нас интересует только верх - низ.
        Мы будем использовать функцию onMove, где между вью холдерами и таргет вью холдером
        будем менять и перемешивать элементы.

        Элементы для перемешивания берем как Адаптер. текущий лист
       */
            override fun onMove(
                recyclerView: RecyclerView,
                startItem: RecyclerView.ViewHolder,
                targetItem: RecyclerView.ViewHolder,
            ): Boolean {
                val tempList = ArrayList<ExerciseModel>(adapter.currentList)
                Collections.swap(tempList, startItem.adapterPosition, targetItem.adapterPosition)
                adapter.submitList(tempList)
                return true

            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int,
            ) {

            }

        }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDelete() {
        Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show()
    }
}