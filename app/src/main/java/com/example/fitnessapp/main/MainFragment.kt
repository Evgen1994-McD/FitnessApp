package com.example.fitnessapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.ui.days.DaysViewModel
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {
    
    private val viewModel: DaysViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(
                    lifecycleOwner = this@MainFragment
                )
            )
            setContent {
                FitnessAppTheme {
                    MainScreenContent(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Загружаем данные при создании фрагмента
        viewModel.getAllBodyTrainingDays()
    }
}

@Composable
private fun MainScreenContent(viewModel: DaysViewModel) {
    var allBodyTrainingDays by remember { mutableStateOf<List<DayModel>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<DayModel>>(emptyList()) }
    
    // Наблюдаем за LiveData
    DisposableEffect(viewModel.allBodyTrainingDays) {
        val observer = androidx.lifecycle.Observer<List<DayModel>> { list ->
            allBodyTrainingDays = list ?: emptyList()
        }
        viewModel.allBodyTrainingDays.observeForever(observer)
        onDispose {
            viewModel.allBodyTrainingDays.removeObserver(observer)
        }
    }
    
    DisposableEffect(viewModel.daysList) {
        val observer = androidx.lifecycle.Observer<List<DayModel>> { list ->
            daysList = list ?: emptyList()
        }
        viewModel.daysList.observeForever(observer)
        onDispose {
            viewModel.daysList.removeObserver(observer)
        }
    }
    
    MainScreen(
        trainingDays = daysList,
        allBodyTrainingDays = allBodyTrainingDays
    )
}

