package com.example.fitnessapp.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.R


@Composable
fun OpenAllTrainingsDialog(
    dialogState: MutableState<Boolean>,
    isLoading: Boolean = false,
    progress: Float = 0f,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {


    AlertDialog(
        onDismissRequest = { if (!isLoading) dialogState.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit()
                    dialogState.value = false
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "Да",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    dialogState.value = false
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "Нет",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
        },
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(if (isLoading) "Открытие доступов..." else "Открыть доступ ко всем тренировкам?")
                
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    )
}
