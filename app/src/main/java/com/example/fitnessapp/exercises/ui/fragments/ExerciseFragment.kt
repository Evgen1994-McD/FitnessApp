package com.example.fitnessapp.exercises.ui.fragments

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
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
        ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед

        currentDay = getDayFromArguments()
        updateExercise() // привязали обсервер вью модели
        updateTime()
        updateToolbar()
        currentDay?.let { model.getExercises(it) } // с помощью let мы сделали так, что если currentDay
            // будет null - То ничего не запустится. А если не будет, то запустится



        binding.bNext.setOnClickListener {
            if (binding.bNext.text.toString() == getString(R.string.Done)) {
findNavController()
    .popBackStack(
        R.id.trainingFragment,
        inclusive = false
    )
    /*
    возвращаемся по бекстеку назад ( стек фрагментов из навигации)
    в функции навконтроллера popBackStack можно указать аргументы
    Например если мы хотим по стопке вернуться не на 1 фрагмент назад, а сразу на начало, указываем куда вернуться
    и указываем сохранять ли те фрагменты с которых мы ушли. В нашем случае - нет
    Так мы не сможем на них вернуться с помощью кнопки назад на смартфоне
     */
            } else {

                model.nextExercise()
            }
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
            setMainColors(
                !subTitle.text.toString().startsWith("Отдохните")
            )
            changeButtonText(exercise.name)
            /*
            устанавливаем нужные цвета если это релакс или нет
             */
            showTime(exercise)
            /*
            С помощью обсервера передаю данные на фрагмент
            Кроме времени и прогресс бара - их будем делать через Таймер
             */


        }
    }

    private fun changeButtonText(title : String){
        if (title == getString(R.string.day_finish_name)){
            binding.bNext.text = getString(R.string.Done)
        }
    }

    private fun updateTime() = with(binding){
        model.updateTime.observe(viewLifecycleOwner){ time ->
            tvTime.text = TimeUtils.getTime(time)
animProgressBar(time)
            /*
            передаём прогресс в прогресс бар
             */
        }
    }

    private fun updateToolbar(){
        model.updateToolbar.observe(viewLifecycleOwner){ text ->
            ab?.title = text

        }
    }






    private fun showTime(exercise: ExerciseModel?) {
        if (exercise?.time!!.startsWith("x") || exercise.time.isEmpty() ) {
//            timer?.cancel() // сбросим таймер если он есть
            binding.progressBar.visibility = View.INVISIBLE  // если количество повторений считаем, то прогрессбар не нужен, поэтому инвизибл
            binding.tvTime.text = exercise.time
        } else {
            binding.progressBar.visibility = View.VISIBLE // тут соответвтенно - нужен Прогрессбар
            binding.progressBar.max = exercise.time.toInt() * 1000 // потому что считаем в милисекундах умножаем на 1000
            binding.progressBar.progress =  exercise?.time!!.toInt() * 1000 // обновим максимум пб До максимума
        model.startTimer(exercise.time.toLong()) // запустим таймер
        }
    }

    private fun setMainColors(isExercise : Boolean)= with(binding){
        val white =ContextCompat.getColor(requireContext(), R.color.white)
        val blue =ContextCompat.getColor(requireContext(), R.color.blue)
        val blueDark =ContextCompat.getColor(requireContext(), R.color.blue_dark)
        val black =ContextCompat.getColor(requireContext(), R.color.black)

        if (isExercise){

             bg.setBackgroundColor(white)
            tvName.setTextColor(black)
            subTitle.setTextColor(blueDark)
            tvTime.setTextColor(black)

progressBar.progressTintList = ColorStateList.valueOf(blueDark)
progressBar.backgroundTintList = ColorStateList.valueOf(white)
            bNext.backgroundTintList = ColorStateList.valueOf(blue)
            bNext.setTextColor(white)

        }else {

            bg.setBackgroundColor(blue)
            tvName.setTextColor(white)
            subTitle.setTextColor(white)
            tvTime.setTextColor(white)

            progressBar.progressTintList = ColorStateList.valueOf(white)
            progressBar.backgroundTintList = ColorStateList.valueOf(white)
            bNext.backgroundTintList = ColorStateList.valueOf(white)
            bNext.setTextColor(black)

        }
    }

    private fun animProgressBar ( restTime : Long) {
       val progressTo= if (restTime>1000){
            restTime - 1000
        } else {
            0
       }

        /*
        выше отрегулировали чтобы не было +1 секунды ( мы так делали потому что
        на экране хотелось видеть 10 секунд отдыха, а если не добавлять то там начинается отсчет
        с 9 секунд
         */
        val anim = ObjectAnimator.ofInt(
            binding.progressBar,
            "progress",
            binding.progressBar.progress,
            progressTo.toInt()
            /*
            ранее умножали на 100 ( в трейнинг фрагменте)
            Здесь же этого не требуется потому что на вход функция принимает
            миллисекунды, а их и так МНОГО
             */
        )
        anim.duration = 700
        anim.start()

        /*
        анимация прогресс бара ( сколько дней сделано)
        умножается на 100 чтобы не было рывков
        duration - за сколько милисекунд дойдём до целевого прогресса
         */
    }



    override fun onPause() {
        super.onPause()
        model.onPause()
        /*
        на паузе сработает одноименная функция во вью модел и наш таймер остановится
         */
    }



}