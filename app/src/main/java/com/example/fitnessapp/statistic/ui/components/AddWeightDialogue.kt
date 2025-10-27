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


    AlertDialog(
        onDismissRequest = { dialogState.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(inputWeight.value.toDouble())
                    dialogState.value = false
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

                TextField(value = inputWeight.value, onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        inputWeight.value = newValue
                    }
                },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            }
        }
    )
}