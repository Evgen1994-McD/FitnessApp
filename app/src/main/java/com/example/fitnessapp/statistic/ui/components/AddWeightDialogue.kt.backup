package com.example.fitnessapp.statistic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.fitnessapp.R


@Composable
fun AddWeightDialogue(
    dialogState: MutableState<Boolean>,
    onSubmit: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val inputWeight = remember { mutableStateOf("") }

    if (dialogState.value) {
        AlertDialog(
            onDismissRequest = { 
                dialogState.value = false
                inputWeight.value = ""
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val weightText = inputWeight.value.trim()
                        if (weightText.isNotEmpty()) {
                            try {
                                val weight = weightText.replace(',', '.').toDouble()
                                if (weight > 0) {
                                    onSubmit(weight)
                                    dialogState.value = false
                                    inputWeight.value = ""
                                }
                            } catch (e: NumberFormatException) {
                                // Не закрываем диалог при ошибке формата
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        dialogState.value = false
                        inputWeight.value = ""
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.weight_input_title))

                    TextField(
                        value = inputWeight.value, 
                        onValueChange = { newValue ->
                            // Разрешаем цифры, точку и запятую
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d+([.,]\\d*)?$"))) {
                                inputWeight.value = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                }
            }
        )
    }
}