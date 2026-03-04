package com.example.fitnessapp.statistic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.R
import com.example.fitnessapp.statistic.ui.models.BMIModel
import com.example.fitnessapp.statistic.ui.models.calculateBMI
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBodyMetricsBottomSheet(
    currentBMI: BMIModel?,
    onDismiss: () -> Unit,
    onSave: (height: Double, weight: Double) -> Unit
) {
    // Инициализируем состояние текущими значениями или значениями по умолчанию
    var height by remember { mutableStateOf(currentBMI?.height?.toString() ?: "170") }
    var weight by remember { mutableStateOf(currentBMI?.weight?.toString() ?: "70") }
    
    // Рассчитываем BMI в реальном времени
    val currentHeight = height.toDoubleOrNull() ?: 170.0
    val currentWeight = weight.toDoubleOrNull() ?: 70.0
    val calculatedBMI = calculateBMI(currentWeight, currentHeight)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Заголовок
            Text(
                text = "Обновите данные тела",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Сохраняйте данные точными для лучших рекомендаций",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Поле для роста
            Text(
                text = "Рост (см)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val current = height.toIntOrNull() ?: 170
                        if (current > 100) height = (current - 1).toString()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Уменьшить",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                OutlinedTextField(
                    value = height,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d{1,3}$"))) {
                            height = newValue
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = {
                        val current = height.toIntOrNull() ?: 170
                        if (current < 250) height = (current + 1).toString()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Увеличить",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Поле для веса
            Text(
                text = "Вес (кг)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val current = weight.toDoubleOrNull() ?: 70.0
                        if (current > 30) {
                            val newValue = current - 0.1
                            weight = String.format(Locale.US, "%.1f", newValue)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Уменьшить",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d+(\\.\\d?)?$"))) {
                            weight = newValue
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = {
                        val current = weight.toDoubleOrNull() ?: 70.0
                        if (current < 200) {
                            val newValue = current + 0.1
                            weight = String.format(Locale.US, "%.1f", newValue)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Увеличить",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            

            Spacer(modifier = Modifier.height(12.dp))

            
            // Расчитанный BMI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "РАССЧИТАННЫЙ ИМТ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format("%.1f %s", calculatedBMI.bmiValue, calculatedBMI.status.displayName),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Информация",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Кнопка сохранения
            Button(
                onClick = {
                    val heightValue = height.toDoubleOrNull() ?: currentBMI?.height ?: 170.0
                    val weightValue = weight.toDoubleOrNull() ?: currentBMI?.weight ?: 70.0
                    onSave(heightValue, weightValue)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_bx_im_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Сохранить изменения",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
