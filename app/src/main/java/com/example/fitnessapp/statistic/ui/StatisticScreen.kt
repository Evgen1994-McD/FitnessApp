package com.example.fitnessapp.statistic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.ui.components.CalendarView
import com.example.fitnessapp.statistic.ui.components.MpAndroidChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    date:String,
    eventList:List<EventDay>,
    onDayClick:()->Unit,
//    tempWeightList:List<WeightModel>,
    label:String
){


Scaffold(
topBar = {
TopAppBar(
    title = { Text(stringResource(R.string.statistic),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold) }
)
}

) {paddingValues ->
    val scrollState = rememberScrollState()

Column(modifier = Modifier
    .padding(paddingValues)
    .fillMaxWidth()
    .verticalScroll(scrollState)
) {

    Text(text = date,
        fontSize = 26.sp,
        modifier = Modifier
            .padding(start = 15.dp,
                top = 30.dp,
                bottom = 50.dp)
    )

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column (modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "00h:00m",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 20.dp)
            )
            Text(text = "TIME")
        }

        Column (modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "0",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 20.dp)

            )
            Text(text = "Kcal")
        }
    }

    Text(text = stringResource(R.string.history),
        fontSize = 26.sp,
        modifier = Modifier
            .padding(start = 15.dp,
                top = 30.dp,
                bottom = 30.dp)
    )
    CalendarView(
        eventList,
        onDayClick)

    Spacer(modifier = Modifier
        .height(50.dp))

    MpAndroidChart(
//        tempWeightList,
        label

    )


}

}
}

//
//
//@Preview(showSystemUi = true)
//@Composable
//fun StatisticPreview(){
//    StatisticScreen(stringResource(R.string.today))
//}