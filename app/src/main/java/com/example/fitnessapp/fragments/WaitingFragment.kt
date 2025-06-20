package com.example.fitnessapp.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.WaitingFragmentBinding
import com.example.fitnessapp.exercises.ui.fragments.ExerciseFragment
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.TimeUtils

const val COUNT_DOWN_TIME = 11000L

class WaitingFragment : Fragment() {
    private lateinit var binding: WaitingFragmentBinding
    private lateinit var timer: CountDownTimer
    private var ab: ActionBar? = null // добавили переменную для ActionBar, будем показывать счетчик упражнений


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WaitingFragmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.Waiting)
        binding.pBar.max = COUNT_DOWN_TIME.toInt() // Установили максимальное значение прогресс бара
        startTimer()
        binding.bSkip.setOnClickListener {
            FragmentManager.setFragment(
                ExerciseFragment.newInstance(),
                activity as AppCompatActivity //запускаем фрагмент с упражнениями если не хотим ждать таймер
            )
            timer.cancel() // останавливаем таймер при нажатии на кнопку
        }
    }

    private fun startTimer() = with(binding) {
        timer = object : CountDownTimer(
            COUNT_DOWN_TIME,
            1
        ) { //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                tvTimer.text =
                    TimeUtils.getTime(restTime) // Рассчитали время которое будет показано в таймере
                pBar.progress = restTime.toInt()

            }

            override fun onFinish() {
                FragmentManager.setFragment(
                    ExerciseFragment.newInstance(),
                    activity as AppCompatActivity //запускаем фрагмент с упражнениями как закончится таймер
                )

            }

        }.start()  // обязательно указываем старт для нашего таймера


    }

    override fun onDetach() {  // Останавливаем таймер если пользователь выходит из фрагмента
        super.onDetach()
        timer.cancel()
    }


    companion object {

        @JvmStatic
        fun newInstance() = WaitingFragment()


    }
}