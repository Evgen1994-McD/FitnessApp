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
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSettingsBinding
import com.example.fitnessapp.databinding.FragmentStatisticBinding
import com.example.fitnessapp.statistic.adapters.DateSelectorAdapter
import com.example.fitnessapp.statistic.data.DateSelectorModel
import com.example.fitnessapp.statistic.utils.UtilsArrays
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.TimeUtils
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
                   if (weight.isNullOrEmpty()){ return
                   } else { try {
                       model.saveWeight(weight.toDouble().toInt())
                   } catch (e: NumberFormatException){
                       Toast.makeText(requireContext(), "Неверный формат", Toast.LENGTH_SHORT ).show()

                   }

                   }
                }

            })
            
        }
        ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед
        ab?.title = "Статистика"
        weightListObserver()
        initRcViews()
        calendarDateObserver()
        statisitcObserver()
        onCalendarClick()
        model.getStatisticEvents()
        model.getStatisticByDate(TimeUtils.getCurrentDate())
        model.getWeightByYearAndMonth()
    }

    private fun initRcViews() = with(binding) {
        val yearList = listOf(
            DateSelectorModel(
                "2022"
            ),
            DateSelectorModel(
                "2023"
            ),
            DateSelectorModel(
                "2024"
            ),
            DateSelectorModel(
                "2025"
            )

        )


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

        val yearTemp = ArrayList<DateSelectorModel>(yearList)
        yearTemp[yearTemp.size-1] =
            yearTemp[yearTemp.size-1].copy(isSelected = true)
        model.year = yearTemp[yearTemp.size-1].text.toInt()

        yearAdapter.submitList(yearTemp)


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

    private fun weightListObserver(){
        model.weightListData.observe(viewLifecycleOwner){ list ->
            list.forEach { weightModel ->
                Log.d("MyLog", "Weight: ${weightModel.weight}")

            }

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