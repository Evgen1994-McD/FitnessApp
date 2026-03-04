package com.example.fitnessapp.statistic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.statistic.ui.models.BMIModel
import com.example.fitnessapp.statistic.ui.models.BMIStatus

@Composable
fun BMICard(
    bmiModel: BMIModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        // Заголовок и статус
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Ваш ИМТ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "На основе ваших данных",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            BMIStatusBadge(status = bmiModel.status)
        }
        
        // Значение ИМТ
        Row(
            modifier = Modifier.padding(vertical = 24.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = String.format("%.1f", bmiModel.bmiValue),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "кг/м²",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }
        
        // Индикатор ИМТ
        BMIIndicator(bmiModel.bmiValue)
    }
}

@Composable
private fun BMIStatusBadge(status: BMIStatus) {
    val backgroundColor = when (status) {
        BMIStatus.UNDERWEIGHT -> Color(0xFF3B82F6)  // blue
        BMIStatus.NORMAL -> Color(0xFF10B981)      // green
        BMIStatus.OVERWEIGHT -> Color(0xFFF59E0B)   // yellow
        BMIStatus.OBESE -> Color(0xFFEF4444)       // red
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = backgroundColor
        )
    }
}

@Composable
private fun BMIIndicator(bmiValue: Double) {
    Column {
        // Фоновая полоса
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            // Цветные сегменты
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .background(Color(0xFF93C5FD)) // blue-300
            )
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .background(Color(0xFF34D399)) // green-400
            )
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .background(Color(0xFFFCD34D)) // yellow-400
            )
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .background(Color(0xFFF87171)) // red-400
            )
        }
        
        // Подписи
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("15", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Text("18.5", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Text("25", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Text("30", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Text("40", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
        }
        
        // Индикатор текущего значения
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            val position = when {
                bmiValue < 18.5 -> (bmiValue - 15.0) / 3.5 * 0.25
                bmiValue < 25 -> 0.25 + (bmiValue - 18.5) / 6.5 * 0.25
                bmiValue < 30 -> 0.5 + (bmiValue - 25.0) / 5.0 * 0.25
                else -> 0.75 + ((bmiValue - 30.0).coerceAtMost(10.0) / 10.0) * 0.25
            }.coerceIn(0.0, 1.0)
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 1.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = (position * 320f).dp - 12.dp)
            )
        }
    }
}
