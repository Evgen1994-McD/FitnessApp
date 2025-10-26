package com.example.fitnessapp.statistic.ui.components

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.applandeo.materialcalendarview.CalendarView

@Composable
fun CalendarView(){
    AndroidView(factory = {
        CalendarView(it).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        }
    })

}





@Preview(showSystemUi = true)
@Composable
fun CalendarPreview(){
    CalendarView()
}