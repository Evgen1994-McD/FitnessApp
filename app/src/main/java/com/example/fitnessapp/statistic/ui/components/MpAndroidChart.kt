package com.example.fitnessapp.statistic.ui.components

import android.app.ActionBar
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.size
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.utils.UtilsArrays
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

@Composable
fun MpAndroidChart(
//    tempWeightList:List<WeightModel>,
                   label:String){
AndroidView(factory = {
    LineChart(it).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

//            setOnChartValueSelectedListener(this@StatisticFragment)
            description.isEnabled = false
            legend.apply {
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                textColor = android.graphics.Color.BLUE
            }
            axisLeft.axisMinimum = 10f
            axisRight.axisMinimum = 10f
            xAxis.apply { // настройки для оси Х ( переместим её вниз)
                position = XAxis.XAxisPosition.BOTTOM
                axisLineColor = android.graphics.Color.BLUE
                gridColor = android.graphics.Color.BLUE
                textColor = android.graphics.Color.BLUE
//                labelCount = tempWeightList.size
                valueFormatter = object : ValueFormatter(){
                    override fun getFormattedValue(value: Float): String {
                        return (value + 1).toInt().toString()


                    }
                }
            }
           axisLeft.apply {
                textColor = android.graphics.Color.BLUE
                gridColor = android.graphics.Color.BLUE

            }
          axisRight.apply {
                textColor = android.graphics.Color.BLUE
                gridColor = android.graphics.Color.BLUE

            }


        val weightList = ArrayList<Entry>()
//        for (i in 0 until 30) {
//            val filteredList = tempWeightList.filter { it.day == i + 1 }
//
//            if (filteredList.isNotEmpty()) {
//                weightList.add(Entry(i.toFloat(), filteredList.first().weight.toFloat()))
//            } else {
//                // Оставляем пустой промежуток или используем среднее предыдущих значений
//                continue // Пропускаем запись
//            }
//        }

        val set: LineDataSet

        if (data != null && data.dataSetCount > 0) {
            set = data.getDataSetByIndex(0) as LineDataSet
            set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            set.values = weightList
//            set.label = "${model.year}/${UtilsArrays.monthList[model.month].text}"
            set.label = label
            data.notifyDataChanged() // сообщаем что данные изменились и перерисуем
            notifyDataSetChanged() // проверяем весь бар чарт на изменения и перерисуем если надо

            /*
            Выше мы проверяем, если есть уже инстанция BarDataSet - мы редактируем её.
            Так как нам нужен только один график, мы можем просто её редактировать, и получаем по индексу
            (графики есди их много идут тоже по индексу ( 0, 1, 2, 3 и так далее)
            Далее уже меняем значения
             */
        } else {

//            set = LineDataSet(weightList, "${model.year}/${UtilsArrays.monthList[model.month].text}") //Это сам график
            set = LineDataSet(weightList, label) //Это сам график
            set.color = android.graphics.Color.GREEN
            set.valueTextColor = android.graphics.Color.RED
            set.valueTextSize = 20f
            set.circleRadius = 3f

            set.lineWidth = 5f
            set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

            val dataSets = ArrayList<ILineDataSet>() //это список с графиками ( у нас если что 1)
            dataSets.add(set) // тот самый один график который мы и добавляем
            val barDate = LineData(dataSets)// Передаём всё в бар дата
            barDate.setValueTextSize(10f) // настраиваем если надо, есть много разных функций
            data =
                barDate // Передали данные в БарДата. В barDate есть вообще все данные, поэтому его и передаём
        }
        invalidate() // перересовываем, обязательно



    }
})

}







//@Preview(showSystemUi = true)
//@Composable
//fun ChartPreview(){
//MpAndroidChart(tempWeightList = listOf(
//    WeightModel(
//        null,
//        80.0,
//        2,
//        1,
//        2025
//    ),
//    WeightModel(
//        null,
//        60.0,
//        5,
//        1,
//        2025
//    )
//),
//    label = "2025")
//}