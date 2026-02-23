package com.example.fitnessapp.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnessapp.exercises.utils.TrainingUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onPlanCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: TrainingPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var goal by remember { mutableStateOf("") }
    var selectedZones by remember { mutableStateOf(setOf<String>()) }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var timePerSession by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать AI план") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Заголовок
                Text(
                    text = "Создание AI плана тренировок",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                // Цель тренировки
                Text(
                    text = "Цель тренировки:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                val goals = listOf(
                    "Похудение" to "похудения",
                    "Набор массы" to "набора массы", 
                    "Рельеф" to "рельефа",
                    "Поддержание формы" to "поддержания формы"
                )
                
                goals.forEach { (display, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = goal == value,
                                onClick = { goal = value },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = goal == value,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = display)
                    }
                }
                
                // Выбор зон
                Text(
                    text = "Целевые зоны:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                val zones = listOf(
                    "hands" to "Руки",
                    "legs" to "Ноги",
                    "body" to "Тело",
                    "back" to "Спина",
                    "press" to "Пресс",
                    "cardio" to "Кардио"
                )
                
                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = zones,
                        key = { (zone, _) -> zone }
                    ) { (zone, displayName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedZones.contains(zone),
                                    onClick = { 
                                        selectedZones = if (selectedZones.contains(zone)) {
                                            selectedZones - zone
                                        } else {
                                            selectedZones + zone
                                        }
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedZones.contains(zone),
                                onCheckedChange = { checked ->
                                    selectedZones = if (checked) {
                                        selectedZones + zone
                                    } else {
                                        selectedZones - zone
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = displayName)
                        }
                    }
                }
                
                // Выбор дней
                Text(
                    text = "Дни тренировок:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                val daysOfWeek = listOf(
                    "1" to "Понедельник",
                    "2" to "Вторник",
                    "3" to "Среда",
                    "4" to "Четверг",
                    "5" to "Пятница",
                    "6" to "Суббота",
                    "7" to "Воскресенье"
                )
                
                LazyColumn(
                    modifier = Modifier.height(140.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = daysOfWeek,
                        key = { (day, _) -> day }
                    ) { (day, displayName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedDays.contains(day),
                                    onClick = { 
                                        selectedDays = if (selectedDays.contains(day)) {
                                            selectedDays - day
                                        } else {
                                            selectedDays + day
                                        }
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedDays.contains(day),
                                onCheckedChange = { checked ->
                                    selectedDays = if (checked) {
                                        selectedDays + day
                                    } else {
                                        selectedDays - day
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = displayName)
                        }
                    }
                }
                
                // Время на тренировку
                Text(
                    text = "Время на тренировку (минут):",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                OutlinedTextField(
                    value = timePerSession,
                    onValueChange = { timePerSession = it },
                    label = { Text("Время") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Назад")
                    }
                    
                    Button(
                        onClick = {
                            if (goal.isNotEmpty() && selectedZones.isNotEmpty() && 
                                selectedDays.isNotEmpty() && timePerSession.isNotEmpty()) {
                                
                                viewModel.generatePlan(
                                    goal = goal,
                                    targetZones = selectedZones.toList(),
                                    availableDays = selectedDays.map { it.toIntOrNull() ?: 1 },
                                    timePerSession = timePerSession.toIntOrNull() ?: 30
                                )
                                onPlanCreated()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = goal.isNotEmpty() && selectedZones.isNotEmpty() && 
                                selectedDays.isNotEmpty() && timePerSession.isNotEmpty() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Создать план")
                        }
                    }
                }
                
                // Сообщения об ошибках и успехе
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (uiState.success != null) {
                    Text(
                        text = "✅ План успешно создан!",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
