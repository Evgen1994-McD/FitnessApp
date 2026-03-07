package com.example.fitnessapp.exercises.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import pl.droidsonroids.gif.GifDrawable
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.utils.ZoneUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseBottomSheet(
    exercise: ExerciseModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp), // Добавляем отступ снизу для кнопок
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Text(
                text = exercise.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // GIF с упражнением большего размера
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            adjustViewBounds = true
                            try {
                                setImageDrawable(GifDrawable(context.assets, exercise.image))
                            } catch (e: Exception) {
                                // Обработка ошибки загрузки GIF
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clipToBounds() // Обрезаем контент, чтобы он не выходил за границы
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Дополнительный отступ после GIF

            // Информация о мышечных зонах
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Мышечные группы:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ZoneUtils.getZonesDisplayNames(exercise.muscleZone),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Описание упражнения
            if (exercise.description.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp), // Увеличиваем отступы для консистентности
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Увеличиваем расстояние между элементами
                    ) {
                        Text(
                            text = "Описание:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = exercise.description,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Инструкция выполнения
            if (exercise.instruction.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp), // Увеличиваем отступы для объемного текста
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Увеличиваем расстояние между элементами
                    ) {
                        Text(
                            text = "Инструкция выполнения:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = exercise.instruction.split("||")
                                .mapIndexed { index, step -> "${index + 1}. ${step.trim()}" }
                                .joinToString("\n"),
                            fontSize = 16.sp,
                            lineHeight = 26.sp, // Увеличиваем межстрочный интервал для лучшей читаемости
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Распространенные ошибки
            if (exercise.mistakes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Распространенные ошибки:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = exercise.mistakes.split("||")
                                .mapIndexed { index, mistake -> "${index + 1}. ${mistake.trim()}" }
                                .joinToString("\n"),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Советы по правильному выполнению
            if (exercise.advise.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Советы по выполнению:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = exercise.advise.split("||")
                                .mapIndexed { index, tip -> "${index + 1}. ${tip.trim()}" }
                                .joinToString("\n"),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
