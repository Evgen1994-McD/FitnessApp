package com.example.fitnessapp.statistic.ui.components

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.fitnessapp.utils.TimeUtils
import java.util.Calendar

@Composable
fun CalendarView(
    eventList:List<EventDay>?,
                 onDayClick:(String) -> Unit){
    AndroidView(factory = {
        CalendarView(it).apply {
            layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            if (eventList!=null) {
                eventList?.let { it1 -> setEvents(it1) }
            }
            setOnDayClickListener(object : OnDayClickListener {
                override fun onDayClick(eventDay: EventDay) {
                    val selectedDate = TimeUtils.getDateFromCalendar(eventDay.calendar)
                    onDayClick(selectedDate)
                }
            })
        }
    })

}

