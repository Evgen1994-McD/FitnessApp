package com.example.fitnessapp.statistic.ui.components

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnessapp.db.WeightModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun MpAndroidChart(
    weightList: List<WeightModel>,
    onWeightClick: (WeightModel) -> Unit
) {
    AndroidView(modifier = Modifier
        .height(300.dp),
        factory = {
            LineChart(it).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let { entry ->
                            val dayNumber = (entry.x + 1).toInt()
                            val weightModel = getWeightModelByDay(dayNumber, weightList)
                            weightModel?.let { onWeightClick(it) }
                        }
                    }

                    override fun onNothingSelected() {
                        // Ничего не делаем
                    }
                })
                
                description.isEnabled = false
                legend.apply {
                    horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    textColor = android.graphics.Color.BLUE
                }
                axisLeft.axisMinimum = 10f
                axisRight.axisMinimum = 10f
                xAxis.apply {
                    axisMaximum = 30f
                    axisMinimum = 1f
                    labelCount = 6
                    position = XAxis.XAxisPosition.BOTTOM
                    axisLineColor = android.graphics.Color.BLUE
                    gridColor = android.graphics.Color.BLUE
                    textColor = android.graphics.Color.BLUE
                    valueFormatter = object : ValueFormatter() {
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
            }
        },
        update = { chart ->
            // Обновляем график при изменении данных
            val chartEntries = ArrayList<Entry>()
            for (i in 0 until 30) {
                val filteredList = weightList.filter { it.day == i + 1 }

                if (filteredList.isNotEmpty()) {
                    chartEntries.add(Entry(i.toFloat(), filteredList.first().weight.toFloat()))
                } else {
                    continue
                }
            }
            if (chartEntries.isNotEmpty()) {
                val minWeight = chartEntries.minOfOrNull { it.y } ?: 0f
                val maxWeight = chartEntries.maxOfOrNull { it.y } ?: 100f

                chart.axisLeft.axisMinimum = minWeight - 5f
                chart.axisLeft.axisMaximum = maxWeight + 5f
                chart.axisRight.axisMinimum = minWeight - 5f
                chart.axisRight.axisMaximum = maxWeight + 5f
            } else {
                // Если нет данных, установить стандартные значения
                chart.axisLeft.axisMinimum = 50f
                chart.axisLeft.axisMaximum = 100f
                chart.axisRight.axisMinimum = 50f
                chart.axisRight.axisMaximum = 100f
            }
       
            val set: LineDataSet
            if (chart.data != null && chart.data.dataSetCount > 0) {
                set = chart.data.getDataSetByIndex(0) as LineDataSet
                set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                set.values = chartEntries
                set.label = "Вес (кг)"
                chart.data.notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                set = LineDataSet(chartEntries, "Вес (кг)")
                set.color = android.graphics.Color.GREEN
                set.valueTextColor = android.graphics.Color.RED
                set.valueTextSize = 20f
                set.circleRadius = 3f
                set.lineWidth = 5f
                set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

                val dataSets = ArrayList<ILineDataSet>()
                dataSets.add(set)
                val lineData = LineData(dataSets)
                lineData.setValueTextSize(10f)
                chart.data = lineData
            }
            chart.invalidate()
        }
    )
}

private fun getWeightModelByDay(day: Int, weightList: List<WeightModel>): WeightModel? {
    val daysList = weightList.filter { it.day == day }
    return if (daysList.isEmpty()) {
        null
    } else {
        daysList[0]
    }
}
