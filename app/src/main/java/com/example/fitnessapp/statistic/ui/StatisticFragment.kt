package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSettingsBinding
import com.example.fitnessapp.databinding.FragmentStatisticBinding
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StatisticFragment : Fragment() {

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
        ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед
ab?.title = "Статистика"
        calendarDateObserver()
        statisitcObserver()
        onCalendarClick()
        model.getStatisticEvents()
        model.getStatisticByDate(TimeUtils.getCurrentDate())
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

    private fun onCalendarClick(){
        binding.cView.setOnDayClickListener(object : OnDayClickListener{
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