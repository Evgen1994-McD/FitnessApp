package com.fit.fitnessapp.statistic.ui.components

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.fit.fitnessapp.utils.TimeUtils

@Composable
fun CalendarView(
    eventList:List<EventDay>?,
                 onDayClick:(String) -> Unit){
    AndroidView(factory = {
        CalendarView(it).apply {
            layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            eventList?.let { setEvents(it) }
            setOnDayClickListener(object : OnDayClickListener {
                override fun onDayClick(eventDay: EventDay) {
                    val selectedDate = TimeUtils.getDateFromCalendar(eventDay.calendar)
                    onDayClick(selectedDate)
                }
            })
        }
    }, update = { calendarView ->
        eventList?.let { calendarView.setEvents(it) }
    })
}

