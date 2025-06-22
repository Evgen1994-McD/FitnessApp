package com.example.fitnessapp.exercises.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ExerciseBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.exercises.ui.ExerciseViewModel
import com.example.fitnessapp.fragments.DaysFinishFragment
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.TimeUtils
import com.example.fitnessapp.utils.getDayFromArguments
import dagger.hilt.android.AndroidEntryPoint
import pl.droidsonroids.gif.GifDrawable
@AndroidEntryPoint // аннотация для того чтобы создать например вью модел
class ExerciseFragment : Fragment() {
    private lateinit var binding: ExerciseBinding
    private val model: ExerciseViewModel by viewModels()
    /*
    если мы укажем viewModels() - то вью модел даггер хилт привяжет ко фрагменту - то есть фрагмент разрушится,
    и вью модел - тоже.

   Если указать activityViewModels() - то вью модел привяжется к циклу жизни активити

   Мы сделали так, чтобы запускать свежие данные - чтобы изюежать багов ( запуск дважды и т.д.)
   Если мы хотим поменяться данными с активити, с другими фрагментами, то имеет смысл привязать к активити.
   А так нет
     */

    private  var currentDay : DayModel? = null   // Это деймодел кооторый мы передали в аргументах
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExerciseBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*
        getDayFromArguments - это получение аргумента ( ДейМодел) который мы передали в Бандл
        и отправили как аргумент с помощью навигации
         */
        super.onViewCreated(view, savedInstanceState)
        currentDay = getDayFromArguments()
        updateExercise() // привязали обсервер вью модели
        updateTime()
        currentDay?.let { model.getExercises(it) } // с помощью let мы сделали так, что если currentDay
            // будет null - То ничего не запустится. А если не будет, то запустится

        ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед

        binding.bNext.setOnClickListener {

            model.nextExercise()
        }


    }

    private fun updateExercise() = with(binding){
        model.updateExercise.observe(viewLifecycleOwner){ exercise ->
            imMine.setImageDrawable(exercise?.image?.let {
                GifDrawable(
                    root.context.assets,
                    exercise.image
                )
            })

            tvName.text = exercise.name
            subTitle.text = exercise.subtitle
            showTime(exercise)
            /*
            С помощью обсервера передаю данные на фрагмент
            Кроме времени и прогресс бара - их будем делать через Таймер
             */


        }
    }

    private fun updateTime() = with(binding){
        model.updateTime.observe(viewLifecycleOwner){ time ->
            tvTime.text = TimeUtils.getTime(time)

        }
    }






    private fun showTime(exercise: ExerciseModel?) {
        if (exercise?.time!!.startsWith("x")) {
//            timer?.cancel() // сбросим таймер если он есть
            binding.progressBar.visibility = View.INVISIBLE  // если количество повторений считаем, то прогрессбар не нужен, поэтому инвизибл
            binding.tvTime.text = exercise.time
        } else {
            binding.progressBar.visibility = View.VISIBLE // тут соответвтенно - нужен Прогрессбар
            binding.progressBar.progress =  exercise?.time!!.toInt() * 1000 // обновим максимум пб До максимума
        model.startTimer(exercise.time.toLong()) // запустим таймер
        }
    }






    override fun onPause() {
        super.onPause()
        model.onPause()
        /*
        на паузе сработает одноименная функция во вью модел и наш таймер остановится
         */
    }



}