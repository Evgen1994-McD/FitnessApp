package com.example.fitnessapp.statistic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                Button(
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onDismiss()
                        dialogState.value = false
                        inputWeight.value = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF757575),
                        contentColor = Color.White
                    )
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
