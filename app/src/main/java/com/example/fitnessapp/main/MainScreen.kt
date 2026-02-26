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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
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

// Вспомогательные функции для оптимизации
private fun calculateProgress(progress: Int, maxProgress: Int): Float {
    return if (maxProgress > 0) {
        progress.toFloat() / maxProgress
    } else 0f
}

// Data классы для оптимизированных данных
data class OptimizedDifficultyCard(
    val difficulty: String,
    val title: String,
    val progress: Float,
    val progressPercent: Int,
    val imageId: Int
)

data class OptimizedZoneCard(
    val key: String,
    val difficulty: String,
    val zone: String,
    val difficultyTitle: String,
    val zoneTitle: String,
    val progress: Float,
    val progressPercent: Int,
    val imageId: Int
)

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
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Предварительные вычисления для оптимизации производительности
            val preparedDifficultyCards = remember(progressMap) {
                listOf(TrainingUtils.EASY, TrainingUtils.MIDDLE, TrainingUtils.HARD)
                    .filter { difficulty -> (progressMap[difficulty]?.maxProgress ?: 0) > 0 }
                    .map { difficulty ->
                        val topCard = progressMap[difficulty]!!
                        val progress = calculateProgress(topCard.progress, topCard.maxProgress)

                        OptimizedDifficultyCard(
                            difficulty = difficulty,
                            title = difficulty, // Установится в UI
                            progress = progress,
                            progressPercent = (progress * 100).toInt(),
                            imageId = topCard.imageId
                        )
                    }
            }

            val preparedZoneCards = remember(progressMap) {
                val zones = listOf(
                    TrainingUtils.HANDS,
                    TrainingUtils.BODY,
                    TrainingUtils.BACK,
                    TrainingUtils.LEGS
                )
                val difficulties =
                    listOf(TrainingUtils.EASY, TrainingUtils.MIDDLE, TrainingUtils.HARD)

                zones.flatMap { zone ->
                    difficulties.map { difficulty -> "${difficulty}_$zone" }
                }.filter { key ->
                    (progressMap[key]?.maxProgress ?: 0) > 0
                }.map { key ->
                    val card = progressMap[key]!!
                    val parts = key.split("_")
                    val difficulty = parts[0]
                    val zone = parts[1]
                    val progress = calculateProgress(card.progress, card.maxProgress)

                    OptimizedZoneCard(
                        key = key,
                        difficulty = difficulty,
                        zone = zone,
                        difficultyTitle = difficulty, // Установится в UI
                        zoneTitle = zone, // Установится в UI
                        progress = progress,
                        progressPercent = (progress * 100).toInt(),
                        imageId = card.imageId
                    )
                }
            }

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
                        text = pluralStringResource(
                            id = R.plurals.workouts_count,
                            count = totalWorkouts
                        ),
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
                val progress = calculateProgress(customCard.progress, customCard.maxProgress)
                val progressPercent = (progress * 100).toInt()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp)
                        .clickable { onStartTrainingClick(TrainingUtils.CUSTOM, null) },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
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
                            Text(
                                text = "Здесь все ваши самостоятельно созданные программы",
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Прогресс внизу
                            Column {
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

            // Карточки по уровню сложности
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                preparedDifficultyCards.forEach { card ->
                    val title = when (card.difficulty) {
                        TrainingUtils.EASY -> stringResource(R.string.easy)
                        TrainingUtils.MIDDLE -> stringResource(R.string.middle)
                        TrainingUtils.HARD -> stringResource(R.string.hard)
                        else -> card.difficulty
                    }

                    AllBodyCard(
                        programName = { stringResource(R.string.all_body) },
                        difficulty = { title },
                        progressText = { "Прогресс: ${card.progressPercent}%" },
                        progress = card.progress,
                        onStartClick = { onStartTrainingClick(card.difficulty, null) },
                        image = card.imageId
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок тренировок по зонам
            Text(
                text = "Тренировки по зонам",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Карточки тренировок по зонам
            preparedZoneCards.forEach { card ->
                val difficultyTitle = when (card.difficulty) {
                    TrainingUtils.EASY -> stringResource(R.string.easy)
                    TrainingUtils.MIDDLE -> stringResource(R.string.middle)
                    TrainingUtils.HARD -> stringResource(R.string.hard)
                    else -> card.difficulty
                }

                val zoneTitle = when (card.zone) {
                    TrainingUtils.HANDS -> stringResource(R.string.hands)
                    TrainingUtils.BODY -> stringResource(R.string.body)
                    TrainingUtils.BACK -> stringResource(R.string.back)
                    TrainingUtils.LEGS -> stringResource(R.string.legs)
                    else -> card.zone
                }

                ZonedTrainingCard(
                    programName = { zoneTitle },
                    difficulty = { difficultyTitle },
                    progressText = { "Прогресс: ${card.progressPercent}%" },
                    progress = card.progress,
                    onStartClick = { onStartTrainingClick(card.difficulty, card.zone) },
                    image = card.imageId
                )
            }
        }
    }
}





