package com.example.fitnessapp.exercises.ui.exercise

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.OptIn
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ExerciseBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.utils.TimeUtils
import com.example.fitnessapp.utils.getDayFromArguments
import dagger.hilt.android.AndroidEntryPoint
import pl.droidsonroids.gif.GifDrawable
@AndroidEntryPoint // аннотация для того чтобы создать например вью модел
class ExerciseFragment : Fragment() {
    private lateinit var binding: ExerciseBinding
    private val model: ExerciseViewModel by viewModels()
    private var totalExerciseCounter = "0"
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
    private var additionalRestTime = 0L // Дополнительное время отдыха для текущей сессии

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



        binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })




        binding.bNext.setOnClickListener {
            if (binding.bNext.text.toString() == getString(R.string.statistic)) {
                var bundle = Bundle()
                bundle.putString("tec", totalExerciseCounter)
                bundle.putString("difficulty", "${currentDay?.difficulty}")
                bundle.putString("zone", "${currentDay?.zone}")
                /*
                В бандл передам диффикульти и зону чтобы на финишном фрагменте, если понадобится,
                изменить все тренировки выбранной сложности и зоны
                 */
                findNavController().navigate(R.id.action_exerciseFragment_to_daysFinishFragment, bundle)

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

        // Обработчик для кнопки +20 сек
        binding.bAddTime?.setOnClickListener {
            // Добавляем 20 секунд только если сейчас отдых
            val isRest = binding.subTitle.text.toString().startsWith(getString(R.string.relax))
            if (isRest) {
                additionalRestTime += 20000L // 20 секунд в миллисекундах
                // Обновляем таймер с учетом дополнительного времени
                model.currentTimerValue?.let { currentTime ->
                    val newTime = currentTime + 20000L
                    model.updateTimerValue(newTime)
                }
            }
        } ?: run {
            // Если кнопка не найдена в binding, пробуем найти по ID
            val addTimeButton = binding.root.findViewById<Button>(R.id.bAddTime)
            addTimeButton?.setOnClickListener {
                val isRest = binding.subTitle.text.toString().startsWith(getString(R.string.relax))
                if (isRest) {
                    additionalRestTime += 20000L
                    model.currentTimerValue?.let { currentTime ->
                        val newTime = currentTime + 20000L
                        model.updateTimerValue(newTime)
                    }
                }
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
                !subTitle.text.toString().startsWith(getString(R.string.relax))
            )
            setPreFinishColors(
                subTitle.text.toString().startsWith(getString(R.string.day_finish_subtitle))

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
            binding.bNext.text = getString(R.string.statistic)
        }
    }

    private fun updateTime() = with(binding){
        model.updateTime.observe(viewLifecycleOwner){ time ->
            val totalTime = time + additionalRestTime
            tvTime.text = TimeUtils.getTime(totalTime)
            animProgressBar(totalTime)
            /*
            передаём прогресс в прогресс бар с учетом дополнительного времени
             */
        }
    }

    private fun updateToolbar(){
        model.updateToolbar.observe(viewLifecycleOwner){ text ->
            ab?.title = text
             totalExerciseCounter = text.split("/")[1]

        }
    }






    private fun showTime(exercise: ExerciseModel?) {
        // Сбрасываем дополнительное время отдыха при каждом новом упражнении
        additionalRestTime = 0L
        
        // Получаем кнопку либо через binding, либо через findViewById
        val addTimeButton = binding.bAddTime ?: binding.root.findViewById<Button>(R.id.bAddTime)
        
        if (exercise?.time!!.startsWith("x") || exercise.time.isEmpty() ) {
            binding.progressBar.visibility = View.INVISIBLE  // если количество повторений считаем, то прогрессбар не нужен, поэтому инвизибл
            binding.tvTime.text = exercise.time
            addTimeButton?.visibility = View.GONE // Скрываем кнопку +20 сек для упражнений с повторениями
        } else {
            binding.progressBar.visibility = View.VISIBLE // тут соответвтенно - нужен Прогрессбар
            binding.progressBar.max = exercise.time.toInt() * 1000 // потому что считаем в милисекундах умножаем на 1000
            binding.progressBar.progress = exercise?.time!!.toInt() * 1000 // обновим максимум пб До максимума
            model.startTimer(exercise.time.toLong()) // запустим таймер
        }
    }

    private fun setMainColors(isExercise : Boolean)= with(binding){
        val background = ContextCompat.getColor(requireContext(), R.color.background)
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
        val white =ContextCompat.getColor(requireContext(), R.color.white)
        val blue =ContextCompat.getColor(requireContext(), R.color.blue)
        val blueDark =ContextCompat.getColor(requireContext(), R.color.blue_dark)
        val black =ContextCompat.getColor(requireContext(), R.color.black)

        // Отладочная информация
        android.util.Log.d("ExerciseFragment", "setMainColors: isExercise=$isExercise")
        android.util.Log.d("ExerciseFragment", "bAddTime exists: ${bAddTime != null}")

        // Получаем кнопку либо через binding, либо через findViewById
        val addTimeButton = bAddTime ?: binding.root.findViewById<Button>(R.id.bAddTime)
        android.util.Log.d("ExerciseFragment", "addTimeButton found: ${addTimeButton != null}")

        if (isExercise){

             bg.setBackgroundColor(background)
            tvName.setTextColor(textColor)
            subTitle.setTextColor(textColor)
            tvTime.setTextColor(textColor)

progressBar.progressTintList = ColorStateList.valueOf(blueDark)
progressBar.backgroundTintList = ColorStateList.valueOf(white)
            bNext.backgroundTintList = ColorStateList.valueOf(blue)
            bNext.setTextColor(white)
            addTimeButton?.visibility = View.GONE // Скрываем кнопку +20 сек для упражнений
            android.util.Log.d("ExerciseFragment", "Button visibility set to GONE")

        }else {

            bg.setBackgroundColor(blue)
            tvName.setTextColor(white)
            subTitle.setTextColor(white)
            tvTime.setTextColor(white)

            progressBar.progressTintList = ColorStateList.valueOf(white)
            progressBar.backgroundTintList = ColorStateList.valueOf(white)
            bNext.backgroundTintList = ColorStateList.valueOf(white)
            bNext.setTextColor(black)
            addTimeButton?.visibility = View.VISIBLE // Показываем кнопку +20 сек для отдыха
            android.util.Log.d("ExerciseFragment", "Button visibility set to VISIBLE")

        }
    }


    private fun setPreFinishColors(isExercise : Boolean)= with(binding){
        val white =ContextCompat.getColor(requireContext(), R.color.white)
        val blue =ContextCompat.getColor(requireContext(), R.color.blue)
        val blueDark =ContextCompat.getColor(requireContext(), R.color.blue_dark)
        val black =ContextCompat.getColor(requireContext(), R.color.black)

        // Получаем кнопку либо через binding, либо через findViewById
        val addTimeButton = bAddTime ?: binding.root.findViewById<Button>(R.id.bAddTime)

        if (isExercise){
            bg.setBackgroundColor(white)
            imMine.visibility = View.INVISIBLE
            lottieView.visibility= View.VISIBLE
            lottieView.playAnimation()


            tvName.setTextColor(black)
            subTitle.setTextColor(blueDark)
            tvTime.setTextColor(black)

            bNext.backgroundTintList = ColorStateList.valueOf(blue)
            bNext.setTextColor(white)
            addTimeButton?.visibility = View.GONE // Скрываем кнопку +20 сек для финишного экрана

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