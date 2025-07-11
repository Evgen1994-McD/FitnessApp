package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSettingsBinding
import com.example.fitnessapp.databinding.FragmentStatisticBinding
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.adapters.DateSelectorAdapter
import com.example.fitnessapp.statistic.data.DateSelectorModel
import com.example.fitnessapp.statistic.utils.UtilsArrays
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.TimeUtils
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import dagger.hilt.android.AndroidEntryPoint
import java.time.Month
import java.util.Calendar
import java.util.Date


@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private lateinit var yearAdapter: DateSelectorAdapter
    private lateinit var monthAdapter: DateSelectorAdapter
    private var _binding: FragmentStatisticBinding? = null //ЭТО сам байндинг Налл
    private val binding get() = _binding!! // а здесь мы получаем байндинг
    private val model: StatisticViewModel by viewModels()
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentStatisticBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    В onDestroyView наш байндинг приравниваем обратно к null
    Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
    но доступ к байдингу всё ещё есть
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.weightEditButton.setOnClickListener {
            DialogManager.showWeightDialog(requireContext(), object : DialogManager.WeightListener {
                override fun onClick(weight: String) {
                    if (weight.isNullOrEmpty()) {
                        return
                    } else {
                        try {
                            model.saveWeight(weight.toDouble().toInt())
                        } catch (e: NumberFormatException) {
                            Toast.makeText(requireContext(), "Неверный формат", Toast.LENGTH_SHORT)
                                .show()

                        }

                    }
                }

            })

        }
        ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед
        ab?.title = "Статистика"
        barChartSettings()
        weightListObserver()
        initRcViews()
        calendarDateObserver()
        statisitcObserver()
        onCalendarClick()
        model.getYearList()

        model.getStatisticEvents()
        model.getStatisticByDate(TimeUtils.getCurrentDate())
        model.getWeightByYearAndMonth()
        observeYearList()

    }

    private fun observeYearList(){
        model.yearListData.observe(viewLifecycleOwner){ list ->
            val yearTemp = ArrayList<DateSelectorModel>(list)
            yearTemp[yearTemp.size - 1] =
                yearTemp[yearTemp.size - 1].copy(isSelected = true)
            model.year = yearTemp[yearTemp.size - 1].text.toInt()

            yearAdapter.submitList(yearTemp)

        }

}

    private fun initRcViews() = with(binding) {

        yearAdapter = DateSelectorAdapter(object : DateSelectorAdapter.Listener {
            override fun onItemClick(index: Int) {
                model.year = yearAdapter.currentList[index].text.toInt()
                setSelectedDateForWeight(index, yearAdapter)
            }

        })
        monthAdapter = DateSelectorAdapter(object : DateSelectorAdapter.Listener {
            override fun onItemClick(index: Int) {
                model.month = index
                setSelectedDateForWeight(index, monthAdapter)

            }

        })
        dateWeightSelector.yearRcView.adapter = yearAdapter
        dateWeightSelector.monthRcView.adapter = monthAdapter



        val monthTemp = ArrayList<DateSelectorModel>(UtilsArrays.monthList)
        monthTemp[Calendar.getInstance().get(Calendar.MONTH)] =
            monthTemp[Calendar.getInstance().get(Calendar.MONTH)].copy(isSelected = true)
        /*
        Интересное решение.
        У нас есть 12 месяцев в массиве.
        Получаем мы их по порядковому номеру из календаря, и изменяем поле isSelected
         */
        monthAdapter.submitList(monthTemp)
    }

    private fun weightListObserver() {
        model.weightListData.observe(viewLifecycleOwner) { list ->

                setChartData(list)




        }
    }


    private fun setSelectedDateForWeight(index: Int, adapter: DateSelectorAdapter) {
        val list = ArrayList<DateSelectorModel>(adapter.currentList)
        for (i in list.indices) {
            list[i] = list[i].copy(isSelected = false)
        }
        list[index] = list[index].copy(isSelected = true)
        adapter.submitList(list)
        model.getWeightByYearAndMonth()
        /*
        Передаём индекс нажатого элемента и адаптер
        Далее при нажатии выбираем текущий лист
        Очищаем всё ( чтобы убрать синее выделение у всего)
        Выделяем выбранный элемент

         for(i in list.indices)  - позволяет пробежать все элементы
         и изменить одно значение ( на false) у всех
         */
    }

    private fun setChartData(tempWeightList: List<WeightModel>) = with(binding) {
        val weightList = ArrayList<BarEntry>()

        for (i in 0..30) {
            val list = tempWeightList.filter {
                i == it.day - 1
            }
            weightList.add(
                BarEntry(
                    i.toFloat(),
                    if (list.isEmpty()) 0f else list[0].weight.toFloat()
                )
            )
        }

        val set: BarDataSet
        if (barChart.data != null && barChart.data.dataSetCount > 0) {
            set = barChart.data.getDataSetByIndex(0) as BarDataSet
            set.values = weightList
            set.label = "${model.year}/${UtilsArrays.monthList[model.month].text}"
            barChart.data.notifyDataChanged() // сообщаем что данные изменились и перерисуем
            barChart.notifyDataSetChanged() // проверяем весь бар чарт на изменения и перерисуем если надо

            /*
            Выше мы проверяем, если есть уже инстанция BarDataSet - мы редактируем её.
            Так как нам нужен только один график, мы можем просто её редактировать, и получаем по индексу
            (графики есди их много идут тоже по индексу ( 0, 1, 2, 3 и так далее)
            Далее уже меняем значения
             */
        } else {

            set = BarDataSet(weightList, "${model.year}/${UtilsArrays.monthList[model.month].text}") //Это сам график
            set.color = android.graphics.Color.GREEN
            val dataSets = ArrayList<IBarDataSet>() //это список с графиками ( у нас если что 1)
            dataSets.add(set) // тот самый один график который мы и добавляем
            val barDate = BarData(dataSets)// Передаём всё в бар дата
            barDate.setValueTextSize(10f) // настраиваем если надо, есть много разных функций
            barChart.data =
                barDate // Передали данные в БарДата. В barDate есть вообще все данные, поэтому его и передаём

        }
        barChart.invalidate() // перересовываем, обязательно

    }

    private fun barChartSettings() {
        binding.apply {
            barChart.description.isEnabled = false
            barChart.legend.apply {
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            }
                barChart.axisLeft.axisMinimum = 10f
                barChart.axisRight.axisMinimum = 10f
            barChart.xAxis.apply { // настройки для оси Х ( переместим её вниз)
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = 6
                valueFormatter = object : ValueFormatter(){
                    override fun getFormattedValue(value: Float): String {
                        return (value + 1).toInt().toString()


                    }
                }
            }
        }
    }


    private fun statisitcObserver() {
        model.statisticData.observe(viewLifecycleOwner) { statisticModel ->
            binding.apply {
                time.text = TimeUtils.getWorkoutTime(
                    statisticModel.workoutTime.toLong() * 1000
                ) //умножили на 1000 потому что время надо в МС
                kcal.text = statisticModel.kcal.toString()
                date.text = if (TimeUtils.getCurrentDate() == statisticModel.date) {
                    "Сегодня"
                } else {
                    statisticModel.date
                }
            }
        }
    }

    private fun onCalendarClick() {
        binding.cView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                model.getStatisticByDate(TimeUtils.getDateFromCalendar(eventDay.calendar))
            }

        })
    }


    private fun calendarDateObserver() {
        model.eventListData.observe(viewLifecycleOwner) { list ->
            binding.cView.setEvents(list)
            /*
            Подписались на вью модел и получение событий для календаря
             */
        }
    }


}