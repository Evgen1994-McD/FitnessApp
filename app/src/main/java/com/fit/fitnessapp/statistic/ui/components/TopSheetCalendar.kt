package com.fit.fitnessapp.statistic.ui.components

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import java.time.LocalDate

@Composable
fun TopSheetCalendar(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    eventList: List<com.applandeo.materialcalendarview.EventDay> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Фон для затемнения и закрытия по клику вне области
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        ) {
            // Сам TopSheet
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                TopSheetContent(
                    onDismiss = onDismiss,
                    onDateSelected = onDateSelected,
                    eventList = eventList,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun TopSheetContent(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    eventList: List<com.applandeo.materialcalendarview.EventDay> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { } // Предотвращаем клики внутри календаря
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой закрытия
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Выбрать неделю",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Календарь
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            CalendarViewComponent(
                onDateSelected = onDateSelected,
                eventList = eventList
            )
        }
    }
}

@Composable
private fun CalendarViewComponent(
    onDateSelected: (LocalDate) -> Unit,
    eventList: List<com.applandeo.materialcalendarview.EventDay> = emptyList()
) {
    val density = LocalDensity.current
    
    AndroidView(
        factory = { context ->
            CalendarView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    with(density) { 400.dp.roundToPx() }
                )
                
                // Устанавливаем события (звездочки)
                setEvents(eventList)
                
                setOnDayClickListener(object : OnDayClickListener {
                    override fun onDayClick(eventDay: com.applandeo.materialcalendarview.EventDay) {
                        val selectedCalendar = eventDay.calendar
                        val selectedDate = LocalDate.of(
                            selectedCalendar.get(java.util.Calendar.YEAR),
                            selectedCalendar.get(java.util.Calendar.MONTH) + 1,
                            selectedCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                        onDateSelected(selectedDate)
                    }
                })
            }
        },
        update = { calendarView ->
            // Обновляем события при изменении
            calendarView.setEvents(eventList)
        }
    )
}

