package com.example.fitnessapp.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.R
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.domain.models.TrainingTopCardModel
import com.example.fitnessapp.exercises.utils.TrainingUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    trainingDays: List<DayModel> = emptyList<DayModel>(),
    progressMap: Map<String, TrainingTopCardModel> = emptyMap(),
    totalWorkouts: Int = 0,
    totalKcal: Int = 0,
    totalTime: String = "00:00",
    onStartTrainingClick: (String, String?) -> Unit = { _, _ -> }
) {
    Scaffold(
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
                        text = totalWorkouts.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pluralStringResource(id = R.plurals.workouts_count, count = totalWorkouts),
                        fontSize = 14.sp
                    )
                }

                // Ккалорий
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalKcal.toString(),
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
                        text = totalTime,
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

            // Карточка кастомных тренировок во всю ширину
            val customCard = progressMap[TrainingUtils.CUSTOM]
            if (customCard != null && customCard.maxProgress > 0) {
                val progress: Float = if (customCard.maxProgress > 0) {
                    customCard.progress.toFloat() / customCard.maxProgress
                } else {
                    0f
                }
                val progressPercent = (progress * 100).toInt()
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp) // Увеличил на 100dp (было 200dp)
                        .padding(horizontal = 16.dp)
                        .clickable { onStartTrainingClick(TrainingUtils.CUSTOM, null) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray // Такой же как у других карточек
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Левая часть с текстом
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Свои тренировки",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(text="Здесь все ваши самостоятельно созданные программы",
                                fontSize = 14.sp)
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Прогресс внизу
                            Column {
                                val progressPercent = (progress * 100).toInt()
                                Text(
                                    text = "Прогресс: $progressPercent%",
                                    fontSize = 16.sp
                                )
                                
                                // Прогресс бар
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .fillMaxHeight(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {}
                                }
                            }
                        }
                        
                        // Правая часть с картинкой
                        Image(
                            painter = painterResource(R.drawable.custom),
                            contentDescription = "Custom Training",
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 16.dp),
                            contentScale = ContentScale.FillHeight
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                    TrainingUtils.HARD
                ).filter { difficulty ->
                    // Отображаем карточку только если в ней есть дни тренировок
                    (progressMap[difficulty]?.maxProgress ?: 0) > 0
                }
                
                items(difficulties) { difficulty ->
                    // Получаем прогресс из Map (используем тот же механизм, что и в TrainingFragment)
                    val topCard = progressMap[difficulty]
                    Log.d("top", "Topcard =$topCard")
                    val progress: Float = if (topCard != null && topCard.maxProgress > 0) {
                        topCard.progress.toFloat() / topCard.maxProgress
                    } else {
                        0f
                    }
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
                        progress = progress,
                        onStartClick = { onStartTrainingClick(difficulty, null) },
                        image = topCard?.imageId ?: R.drawable.ic_custom_training_24
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Тренировки по зонам",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

            ) {
                val zones = listOf(
                    TrainingUtils.HANDS,
                    TrainingUtils.BODY,
                    TrainingUtils.BACK,
                    TrainingUtils.LEGS
                )
                val difficulties = listOf(
                    TrainingUtils.EASY,
                    TrainingUtils.MIDDLE,
                    TrainingUtils.HARD
                )
                
                // Создаем список всех комбинаций зона+сложность
                val zoneTrainingKeys = zones.flatMap { zone -> 
                    difficulties.map { difficulty -> "${difficulty}_$zone" } 
                }.filter { key -> 
                    // Отображаем только те карточки, где есть дни тренировок
                    (progressMap[key]?.maxProgress ?: 0) > 0
                }

                items(zoneTrainingKeys) { key ->
                    val card = progressMap[key]
                    val parts = key.split("_")
                    val difficulty = parts[0]
                    val zone = parts[1]
                    
                    val progress: Float = if (card != null && card.maxProgress > 0) {
                        card.progress.toFloat() / card.maxProgress
                    } else {
                        0f
                    }
                    val progressPercent = (progress * 100).toInt()

                    ZonedTrainingCard(
                        programName = { 
                            when(zone) {
                                TrainingUtils.HANDS -> stringResource(R.string.hands)
                                TrainingUtils.BODY -> stringResource(R.string.body)
                                TrainingUtils.BACK -> stringResource(R.string.back)
                                TrainingUtils.LEGS -> stringResource(R.string.legs)
                                else -> zone
                            }
                        },
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
                        progress = progress,
                        onStartClick = { onStartTrainingClick(difficulty, zone) },
                        image = card?.imageId ?: R.drawable.ic_custom_training_24


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



