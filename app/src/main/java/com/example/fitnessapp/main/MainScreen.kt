package com.example.fitnessapp.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.R
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.utils.TrainingUtils
import com.example.fitnessapp.main.AllBodyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    trainingDays: List<DayModel> = emptyList<DayModel>(),
    allBodyTrainingDays: List<DayModel> = emptyList()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Главное меню",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Row со статистикой
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Тренировок всего
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Тренировок",
                        fontSize = 14.sp
                    )
                }

                // Ккалорий
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ккалорий",
                        fontSize = 14.sp
                    )
                }

                // Время
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Время",
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Row с квадратными карточками (прокручиваемые)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val difficulties = listOf(
                    TrainingUtils.EASY, 
                    TrainingUtils.MIDDLE, 
                    TrainingUtils.HARD,
                    TrainingUtils.CUSTOM
                )
                
                items(difficulties) { difficulty ->
                    val daysForDifficulty = allBodyTrainingDays.filter { 
                        it.difficulty == difficulty 
                    }
                    val progress = calculateProgress(daysForDifficulty)
                    val progressPercent = (progress * 100).toInt()
                    
                    AllBodyCard(
                        programName = { stringResource(R.string.all_body) },
                        difficulty = { 
                            when (difficulty) {
                                TrainingUtils.EASY -> stringResource(R.string.easy)
                                TrainingUtils.MIDDLE -> stringResource(R.string.middle)
                                TrainingUtils.HARD -> stringResource(R.string.hard)
                                TrainingUtils.CUSTOM -> stringResource(R.string.custom)
                                else -> difficulty
                            }
                        },
                        progressText = { "Прогресс: $progressPercent%" },
                        progress = progress
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trainingDays) { day ->
                    TrainingCard(
                        day = day,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            }
        }
    }
}

// Функция для вычисления прогресса
private fun calculateProgress(days: List<DayModel>): Float {
    if (days.isEmpty()) return 0f
    val completed = days.count { it.isDone }
    return completed.toFloat() / days.size
}

@Composable
fun TrainingCard(
    day: DayModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "День ${day.dayNumber}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Сложность: ${day.difficulty}",
                fontSize = 14.sp
            )
        }
    }
}




//@Composable
//@Preview(showSystemUi = true, showBackground = true)
//private fun MainPreview(){
//    MainScreen()
//}