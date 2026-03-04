package com.example.fitnessapp.statistic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.statistic.ui.models.WorkoutHistoryModel

@Composable
fun WorkoutHistoryCard(
    workout: WorkoutHistoryModel,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneIcon = getZoneIcon(workout.zone)
    val zoneColor = getZoneColor(workout.zone)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .let { if (workout.isExpanded) it else it.clickable { onToggleExpand() } }
    ) {
        // Основная информация карточки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка и информация о тренировке
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(zoneColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = zoneIcon,
                        contentDescription = null,
                        tint = zoneColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = getWorkoutTitle(workout.zone, workout.difficulty),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatDate(workout.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Калории и кнопка раскрытия
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${workout.caloriesBurned.toInt()} ккал",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (workout.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (workout.isExpanded) "Свернуть" else "Развернуть",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Раскрытый список упражнений
        AnimatedVisibility(
            visible = workout.isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ExerciseListDetail(
                exercises = workout.exercises,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ExerciseListDetail(
    exercises: List<ExerciseModel>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Разделитель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(bottom = 12.dp)
        )
        
        // Список упражнений
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Иконки упражнений (показываем первые 3)
            exercises.take(3).forEach { exercise ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Текст о количестве упражнений
            Text(
                text = "${exercises.size} упражнений завершено",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

private fun getWorkoutTitle(zone: String?, difficulty: String): String {
    return if (zone.isNullOrEmpty()) {
        "Все тело • $difficulty"
    } else {
        "$zone • $difficulty"
    }
}

private fun getZoneIcon(zone: String?): ImageVector {
    return when (zone?.lowercase()) {
        "руки" -> Icons.Default.FitnessCenter
        "ноги" -> Icons.Default.DirectionsRun
        "спина" -> Icons.Default.SelfImprovement
        "кардио" -> Icons.Default.DirectionsRun
        else -> Icons.Default.FitnessCenter
    }
}

private fun getZoneColor(zone: String?): Color {
    return when (zone?.lowercase()) {
        "руки" -> Color(0xFFF97316) // orange
        "ноги" -> Color(0xFF3B82F6) // blue
        "спина" -> Color(0xFF8B5CF6) // purple
        "кардио" -> Color(0xFF10B981) // green
        else -> Color(0xFFF97316) // default orange
    }
}

private fun formatDate(date: String): String {
    // Простое форматирование даты, можно улучшить
    return date // пока возвращаем как есть, можно добавить парсинг и форматирование
}
