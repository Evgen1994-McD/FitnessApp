package com.fit.fitnessapp.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePlanFragment : Fragment() {
    
    private val viewModel: TrainingPlanViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CreatePlanScreen(
                    viewModel = viewModel,
                    onBack = {
                        findNavController().navigateUp()
                    },
                    onPlanCreated = {
                        findNavController().navigateUp()
                    }
                )
            }
        }
    }
}
