package com.example.fitnessapp.settings.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun RecoveryTimeDialog(
    dialogState: Boolean,
    currentRecoveryTime: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!dialogState) return

    var inputValue by remember { mutableStateOf(currentRecoveryTime.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val time = inputValue.toIntOrNull()
                    if (time != null && time in 1..Int.MAX_VALUE) {
                        onTimeSelected(time)
                        onDismiss()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(
                    text = "ОК",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(
                    text = "Отмена",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        title = {
            Text(
                text = "Время отдыха между упражнениями",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            TextField(
                value = inputValue,
                onValueChange = { newValue ->
                    inputValue = newValue
                    isError = false
                },
                label = {
                    Text("Введите время в секундах")
                },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.onSurface
                ),
                supportingText = {
                    if (isError) {
                        Text(
                            text = "Введите целое число желаемого отдыха в секундах ",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    )
}
