package com.fit.fitnessapp.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiChatFragment : Fragment() {
    
    private val viewModel: AiViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AiChatScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            parentFragmentManager.popBackStack()
                        },
                        onCreatePlanClick = {

                        }
                    )
                }
            }
        }
    }
}
