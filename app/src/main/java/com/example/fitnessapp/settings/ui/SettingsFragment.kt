package com.example.fitnessapp.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.settings.ui.OpenAllTrainingsDialog
import com.example.fitnessapp.settings.ui.SettingsScreen
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val model: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            // Обязательно: стратегия уничтожения композиции
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = this@SettingsFragment))

            setContent {
                FitnessAppTheme {
                    val isOpeningTrainings by model.isOpeningTrainings.observeAsState(initial = false)
                    val openTrainingsProgress by model.openTrainingsProgress.observeAsState(initial = 0f)
                    
                    var clearDialogState by remember { mutableStateOf(false) }
                    var openTrainingsDialogState by remember { mutableStateOf(false) }

                    SettingsScreen(
                        viewModel = model,
                        onClearedDataClick = {
                            clearDialogState = true
                        },
                        onOpenAllTrainingsClick = {
                            openTrainingsDialogState = true
                        }
                    )
                    
                    // Диалог очистки данных
                    if (clearDialogState){
                        ClearDataDialogue(dialogState = remember { mutableStateOf(clearDialogState) },
                            onSubmit ={
                                model.clearData()
                                clearDialogState = false
                            },
                            onDismiss = {
                                clearDialogState = false
                            })
                    }
                    
                    // Диалог открытия всех тренировок
                    if (openTrainingsDialogState){
                        OpenAllTrainingsDialog(
                            dialogState = remember { mutableStateOf(openTrainingsDialogState) },
                            isLoading = isOpeningTrainings,
                            progress = openTrainingsProgress,
                            onSubmit = {
                                model.openAllTrainings()
                            },
                            onDismiss = {
                                openTrainingsDialogState = false
                            })
                        
                        // Автоматически закрываем диалог после завершения операции
                        LaunchedEffect(isOpeningTrainings) {
                            if (!isOpeningTrainings && openTrainingsProgress == 1f) {
                                openTrainingsDialogState = false
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Устанавливаем заголовок
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Настройки"
    }
}