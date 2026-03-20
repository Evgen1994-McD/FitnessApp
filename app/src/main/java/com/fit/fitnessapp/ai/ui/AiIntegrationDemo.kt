package com.fit.fitnessapp.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Демонстрационный компонент для интеграции AI планов
 * Можно добавить в существующее навигационное меню
 */
@Composable
fun AiIntegrationDemo(
    onCreatePlan: () -> Unit,
    onViewActivePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🤖 AI Планы тренировок",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Создавайте персонализированные планы с помощью искусственного интеллекта",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Button(
            onClick = onCreatePlan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать новый план")
        }
        
        OutlinedButton(
            onClick = onViewActivePlan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Мой активный план")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text(
            text = "Возможности:",
            style = MaterialTheme.typography.titleSmall
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("• Персонализация по целям и зонам")
            Text("• Учет доступных дней и времени")
            Text("• Автоматический подбор упражнений")
            Text("• Рекомендации на основе прогресса")
        }
    }
}
