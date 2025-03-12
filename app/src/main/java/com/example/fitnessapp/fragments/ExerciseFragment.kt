package com.example.fitnessapp.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DayModel
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.adapter.ExerciseAdapter
import com.example.fitnessapp.adapter.ExerciseModel
import com.example.fitnessapp.databinding.ExerciseBinding
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.databinding.FragmentDaysBinding
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.MainViewModel
import com.example.fitnessapp.utils.TimeUtils
import pl.droidsonroids.gif.GifDrawable
import java.util.zip.Inflater

class ExerciseFragment : Fragment() {
    private lateinit var binding: ExerciseBinding
    private val model: MainViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
private var exerciseCounter = 0
    private var timer : CountDownTimer? = null // переменная для таймера
    private var exList: ArrayList<ExerciseModel>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExerciseBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
model.mutableListExercise.observe(viewLifecycleOwner){  // Тут мы получаем список который создали ранее, посредси
exList = it
    nextExercise()
}
        binding.bNext.setOnClickListener { // Запускаем функцию следующего упражнения
            nextExercise()
        }
    }


    private fun nextExercise(){ // функция которая запускает следующее упражнение
        if(exerciseCounter < exList?.size!!){
            timer?.cancel() // сбросили таймер, была проблема что в каждом упражнении начинался таймер после того как один раз он появился
            val ex = exList?.get(exerciseCounter++) ?: return // увеличиваем счётчик упражнений. через оператор элвиса нам или выдает упражнение или ретарн если его нету
            showExercise(ex)
            setExerciseType(ex)
            showNextExercise(ex)
        } else{
            Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show()
        }

    }
    private fun showExercise(exercise : ExerciseModel?) = with(binding){
        if (exercise == null) return@with // если нулл, то ретурн
        imMine.setImageDrawable(exercise?.image?.let { GifDrawable(root.context.assets, exercise.image) })
        tvName.text = exercise.name
    }

    private fun setExerciseType ( exercise: ExerciseModel?){
        if (exercise?.time!!.startsWith("x")){
            binding.tvTime.text = exercise.time
        } else {
            startTimer(exercise)
        }
    }

    private fun showNextExercise(exercise: ExerciseModel?) = with(binding){ // функция которая запускает упражнение которое будет следом за текущим
        if(exerciseCounter < exList?.size!!){
            val ex = exList?.get(exerciseCounter) ?: return
      imNext.setImageDrawable(GifDrawable(root.context.assets, ex.image))
            setTimeType(ex)
        } else{
            imNext.setImageDrawable(GifDrawable(root.context.assets, "congrats.gif")) // Передаём напрямую гиф с поздравлениями, если упражнений больше нет
        tvNextName.text = getString(R.string.GoodDone)
        }

    }

    private fun setTimeType(ex: ExerciseModel){
        if (ex.time.startsWith("x")){
            binding.tvNextName.text = ex.time
        } else {
            val name = ex.name + ": ${TimeUtils.getTime(ex.time.toLong()*1000)}"
            binding.tvNextName.text = name
        }
    }


    private fun startTimer(exercise: ExerciseModel?) = with(binding) {

        progressBar.max =

            exercise?.time!!.toInt() * 1000  // установили максимум прогресс бара ( откуда начали отсчет. На 1000 умножили потому что в миллисекунды
        timer?.cancel() // отключили таймер чтобы не запускался предыдущий на всякий случай
        timer = object : CountDownTimer(
            exercise.time.toLong()*1000, 1
        ) { //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                tvTime.text =
                    TimeUtils.getTime(restTime) // Рассчитали время которое будет показано в таймере
                progressBar.progress = restTime.toInt()

            }

            override fun onFinish() {
                nextExercise()
            } // тут мы переделали, он не вызывает фрагмент, а запускает следующее упражнение при завершении таймера
        }.start()  // обязательно указываем старт для нашего таймера
    }

    override fun onDetach() {
        super.onDetach()
        timer?.cancel() // таймер выключается при выходе из приложения, иначе будут проблемы
    }


        companion object {

        @JvmStatic
        fun newInstance() = ExerciseFragment()


    }
}