package com.example.fitnessapp.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlanScreen(
    onBack: () -> Unit,
    viewModel: TrainingPlanViewModel = hiltViewModel()
) {
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
    val plannedDays by viewModel.plannedDays.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Активный план",
                style = MaterialTheme.typography.headlineMedium
            )
            
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
        }
        
        activePlan?.let { plan ->
            // Информация о плане
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Упражнений в день: ${plan.exercisesPerDay}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (plan.aiGenerated) {
                        Text(
                            text = "🤖 Сгенерировано AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = "Создан: ${dateFormat.format(Date(plan.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Список запланированных дней
            Text(
                text = "План тренировок:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(plannedDays) { day ->
                    DayPlanCard(
                        day = day,
                        onLoadRecommendations = { zone ->
                            viewModel.loadRecommendations(zone)
                        }
                    )
                }
            }
            
            // Рекомендации упражнений
            if (recommendations.isNotEmpty()) {
                Text(
                    text = "Рекомендуемые упражнения:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(recommendations) { exercise ->
                        ExerciseRecommendationCard(exercise = exercise)
                    }
                }
            }
        } ?: run {
            // Нет активного плана
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Нет активного плана",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "Создайте новый план с помощью AI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPlanCard(
    day: com.example.fitnessapp.db.PlannedDayModel,
    onLoadRecommendations: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "День ${day.dayNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (day.restDay) {
                    Text(
                        text = "Отдых",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${day.estimatedTime} мин",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            day.targetZone?.let { zone ->
                Text(
                    text = "Зона: $zone",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Button(
                    onClick = { onLoadRecommendations(zone) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Показать рекомендации")
                }
            }
            
            if (!day.restDay) {
                Text(
                    text = "Упражнений: ${day.exerciseIds.split(",").size}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "~${day.estimatedCalories.toInt()} ккал",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ExerciseRecommendationCard(
    exercise: com.example.fitnessapp.db.ExerciseModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = exercise.muscleZone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
