package com.example.fitnessapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.domain.models.TrainingTopCardModel
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
                    MainScreenContent(viewModel, this@MainFragment)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Загружаем данные при создании фрагмента
        viewModel.loadAllBodyProgress()
    }
}

@Composable
private fun MainScreenContent(viewModel: DaysViewModel, fragment: Fragment) {
    var progressMap by remember { 
        mutableStateOf<Map<String, TrainingTopCardModel>>(
            viewModel.allBodyProgressMap.value ?: emptyMap()
        ) 
    }
    
    // Загружаем данные при первом запуске
    LaunchedEffect(Unit) {
        viewModel.loadAllBodyProgress()
    }
    
    // Наблюдаем за LiveData
    DisposableEffect(viewModel.allBodyProgressMap) {
        val observer = androidx.lifecycle.Observer<Map<String, TrainingTopCardModel>> { map ->
            progressMap = map ?: emptyMap()
        }
        viewModel.allBodyProgressMap.observeForever(observer)
        onDispose {
            viewModel.allBodyProgressMap.removeObserver(observer)
        }
    }
    
    MainScreen(
        trainingDays = emptyList(), // Не передаем daysList, так как он обновляется при навигации к тренировке
        progressMap = progressMap,
        onStartTrainingClick = { difficulty, zone ->
            val bundle = Bundle().apply {
                putString("difficulty", difficulty)
                if (zone != null) {
                    putString("zone", zone)
                }
            }
            fragment.findNavController().navigate(R.id.trainingListFragment, bundle)
        }
    )
}

