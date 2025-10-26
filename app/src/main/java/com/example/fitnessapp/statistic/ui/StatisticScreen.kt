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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.ui.components.CalendarView
import com.example.fitnessapp.statistic.ui.components.DateSelector
import com.example.fitnessapp.statistic.ui.components.MpAndroidChart
import com.example.fitnessapp.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    date: String,
    eventList: List<EventDay>,
    weightList: List<WeightModel>,
    statisticData: StatisticModel?,
    selectedYear: Int,
    selectedMonth: Int,
    onDayClick: () -> Unit,
    onWeightClick: (WeightModel) -> Unit,
    addWeightClick: (WeightModel) -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit
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

    Text(text = if (date == TimeUtils.getCurrentDate()) "Сегодня" else date,
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
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column (modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = statisticData?.let { 
                    TimeUtils.getWorkoutTime(it.workoutTime.toLong() * 1000)
                } ?: "00h:00m",
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
                text = statisticData?.kcal?.toInt()?.toString() ?: "0",
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
        .height(20.dp))
    
    // Добавляем DateSelector перед графиком
    DateSelector(
        selectedYear = selectedYear,
        selectedMonth = selectedMonth,
        onYearChange = onYearChange,
        onMonthChange = onMonthChange
    )
    
    Spacer(modifier = Modifier
        .height(20.dp))
    
    Row(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = {
            addWeightClick(WeightModel(
                null,
                weight = 81.0,
                26,
                11,
                2025
            ))

        },
            modifier = Modifier
                .size(64.dp)){
           Icon(painter = painterResource(R.drawable.ic_add_weight_24),
               contentDescription = null)
        }
    }
    MpAndroidChart(
        weightList = weightList,
        onWeightClick = onWeightClick
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