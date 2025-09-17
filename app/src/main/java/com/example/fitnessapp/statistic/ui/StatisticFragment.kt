package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.fitnessapp.databinding.FragmentStatisticBinding
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.ui.adapters.DateSelectorAdapter
import com.example.fitnessapp.statistic.data.DateSelectorModel
import com.example.fitnessapp.statistic.utils.UtilsArrays
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.TimeUtils
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StatisticFragment : Fragment(), OnChartValueSelectedListener {
    private var currentWeightList : List<WeightModel>? = null
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
                            model.saveWeight(weight.toDouble())
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
        model.getMonthList()
        model.getStatisticEvents()
        model.getStatisticByDate(TimeUtils.getCurrentDate())
        observeYearList()
        observeMonthList()

    }

    private fun observeYearList(){
        model.yearListData.observe(viewLifecycleOwner){ list ->
            val yearTemp = ArrayList<DateSelectorModel>(list)
            yearTemp[yearTemp.size - 1] =
                yearTemp[yearTemp.size - 1].copy(isSelected = true)
            model.year = yearTemp[yearTemp.size - 1].text.toInt()
            yearAdapter.submitList(yearTemp)
            model.getWeightByYearAndMonth()

        }

}

    private fun observeMonthList(){
        model.monthListData.observe(viewLifecycleOwner){ list ->
            val monthTemp = ArrayList<DateSelectorModel>(list)
            monthTemp[monthTemp.size - 1] =
                monthTemp[monthTemp.size - 1].copy(isSelected = true)
            model.month = monthTemp[monthTemp.size - 1].text.toInt()
            val monthToText = monthTemp.map { currentItem ->
                val index = currentItem.text.toIntOrNull()
                if (index != null && index >= 0 && index < UtilsArrays.monthList.size) {
                    currentItem.copy(text = UtilsArrays.monthList[index].text)
                } else {
                    currentItem // Оставляем текущий элемент неизменённым, если индекс вышел за диапазон
                }
            }
            monthAdapter.submitList(monthToText)
            model.getWeightByYearAndMonth()

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
                val monthName = monthAdapter.currentList[index].text
                val findMonthIndex = UtilsArrays.monthList.indexOfFirst { it.text == monthName }
                model.month = findMonthIndex
                setSelectedDateForWeight(index, monthAdapter)

            }

        })
        dateWeightSelector.yearRcView.adapter = yearAdapter
        dateWeightSelector.monthRcView.adapter = monthAdapter



        val monthTemp = ArrayList<DateSelectorModel>(UtilsArrays.monthList)
        monthTemp[model.month] =
            monthTemp[model.month].copy(isSelected = true)
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
            currentWeightList = list




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
        val weightList = ArrayList<Entry>()
        for (i in 0 until 30) {
            val filteredList = tempWeightList.filter { it.day == i + 1 }

            if (filteredList.isNotEmpty()) {
                weightList.add(Entry(i.toFloat(), filteredList.first().weight.toFloat()))
            } else {
                // Оставляем пустой промежуток или используем среднее предыдущих значений
                continue // Пропускаем запись
            }
        }

        val set: LineDataSet


        if (barChart.data != null && barChart.data.dataSetCount > 0) {
            set = barChart.data.getDataSetByIndex(0) as LineDataSet
            set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
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

            set = LineDataSet(weightList, "${model.year}/${UtilsArrays.monthList[model.month].text}") //Это сам график
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
            barChart.data =
                barDate // Передали данные в БарДата. В barDate есть вообще все данные, поэтому его и передаём
        }
        barChart.invalidate() // перересовываем, обязательно

    }

    private fun barChartSettings() {
        binding.apply {
            barChart.setOnChartValueSelectedListener(this@StatisticFragment)
            barChart.description.isEnabled = false
            barChart.legend.apply {
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                textColor = android.graphics.Color.BLUE
            }
                barChart.axisLeft.axisMinimum = 10f
                barChart.axisRight.axisMinimum = 10f
            barChart.xAxis.apply { // настройки для оси Х ( переместим её вниз)
                position = XAxis.XAxisPosition.BOTTOM
                axisLineColor = android.graphics.Color.BLUE
                gridColor = android.graphics.Color.BLUE
                textColor = android.graphics.Color.BLUE
                labelCount = barChart.size
                valueFormatter = object : ValueFormatter(){
                    override fun getFormattedValue(value: Float): String {
                        return (value + 1).toInt().toString()


                    }
                }
            }
            barChart.axisLeft.apply {
                textColor = android.graphics.Color.BLUE
                gridColor = android.graphics.Color.BLUE

            }
            barChart.axisRight.apply {
                textColor = android.graphics.Color.BLUE
                gridColor = android.graphics.Color.BLUE

            }
        }
    }


    private fun statisitcObserver() {
        model.statisticData.observe(viewLifecycleOwner) { statisticModel ->
            binding.apply {
                time.text =
                    TimeUtils.getWorkoutTime(
                    statisticModel.workoutTime.toLong() * 1000
                ) //умножили на 1000 потому что время надо в МС
                kcal.text = statisticModel.kcal.toInt().toString()
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

    override fun onValueSelected(
        e: Entry?,
        h: Highlight?,
    ) {
        val dayNumber =(( e as Entry).x + 1).toInt() // + 1 из за форматтера так как массивы и график начинается с 0
        val weightModel = getWeightModelByDay(dayNumber) ?: return

        DialogManager.showWeightDialog(
            requireContext(),
            object : DialogManager.WeightListener{
                override fun onClick(weight: String) {
                    model.updateWeight(weightModel.copy(
                        weight = weight.toDouble()
                    ))

                }

            },
            weightModel.weight.toString()
        )
    }

    override fun onNothingSelected() {

    }


    private fun getWeightModelByDay(day: Int) : WeightModel? {
        val daysList = currentWeightList?.filter {
            it.day == day
        }
        return if (daysList.isNullOrEmpty() ){
            null
        } else
        {
            daysList[0]
        }
    }

}