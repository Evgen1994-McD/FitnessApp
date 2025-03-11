package com.example.fitnessapp.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DayModel
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.adapter.ExerciseAdapter
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.databinding.FragmentDaysBinding
import com.example.fitnessapp.databinding.WaitingFragmentBinding
import com.example.fitnessapp.utils.MainViewModel
import java.util.zip.Inflater
const val COUNT_DOWN_TIME = 11000L
class WaitingFragment : Fragment() {
    private lateinit var binding: WaitingFragmentBinding
    private lateinit var timer: CountDownTimer

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
binding.pBar.max = COUNT_DOWN_TIME.toInt() // Установили максимальное значение прогресс бара
    }

    private fun startTimer() = with(binding) {
        timer = object : CountDownTimer(COUNT_DOWN_TIME, 100){ //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                pBar.progress = restTime.toInt()

            }

            override fun onFinish() {
                TODO("Not yet implemented")
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