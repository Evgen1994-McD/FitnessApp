package com.fit.fitnessapp.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.fit.fitnessapp.R


@Composable
fun ClearDataDialogue(
    dialogState: MutableState<Boolean>,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {


    AlertDialog(
        onDismissRequest = { dialogState.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit()
                    dialogState.value = false
                }
            ) {
                Text(
                    text = "OK",
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
                }
            ) {
                Text(
                    text = "Cancel",
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
                Text(stringResource(R.string.reset_days_message))

            }
        }
    )
}