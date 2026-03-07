package com.example.fitnessapp.main

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.R
import com.example.fitnessapp.ui.theme.AllBodyCardBgColor
import com.example.fitnessapp.ui.theme.AllBodyCardProgressTrackColor
import com.example.fitnessapp.ui.theme.AllBodyCardTextColor
import com.example.fitnessapp.ui.theme.FitnessAppTheme

@Composable
fun ZonedTrainingCard(
    programName: @Composable () -> String,
    difficulty: @Composable () -> String,
    progressText: () -> String,
    progress: Float,
    onStartClick: () -> Unit = {},
    image: Int

) {
    // Третья карточка
    Card(
        modifier = Modifier
            .padding(top = 12.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(130.dp), // Увеличил высоту, чтобы влезла кнопка
        onClick = {onStartClick()},
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = AllBodyCardBgColor()
        )
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = "Программа:",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = AllBodyCardTextColor()
                    )
                    Text(
                        text = programName(),
                        color = AllBodyCardTextColor()
                    )
                    Text(
                        text = "Сложность:",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = AllBodyCardTextColor()
                    )
                    Text(
                        text = difficulty(),
                        color = AllBodyCardTextColor()
                    )
                    Text(
                        text = progressText(),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = AllBodyCardTextColor()
                    )
                    LinearProgressIndicator(
                        color = Color.Blue,
                        trackColor = AllBodyCardProgressTrackColor(),
                        progress = progress,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(80.dp)
                            .height(8.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(image), contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }

            }
        }

    }

    }




