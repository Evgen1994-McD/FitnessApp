package com.example.fitnessapp.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.ai.ui.CreatePlanScreen
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
