package com.example.fitnessapp.exercises.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ExerciseBinding
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.fragments.DaysFinishFragment
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.MainViewModel
import com.example.fitnessapp.utils.TimeUtils
import pl.droidsonroids.gif.GifDrawable

class ExerciseFragment : Fragment() {
    private lateinit var binding: ExerciseBinding
    private val model: MainViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private var exerciseCounter = 0 // отсюда будем брать данные для сохранения в sharedpref
    private var timer: CountDownTimer? = null // переменная для таймера
    private var exList: ArrayList<ExerciseModel>? = null
    private var currentDay = 0
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
currentDay = model.currentDay
        super.onViewCreated(view, savedInstanceState)
        exerciseCounter =
            model.getExerciseCount() // тут мы вызвваем упражнения на котором остановились из SharedPreferences
        ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед
        model.mutableListExercise.observe(viewLifecycleOwner) {  // Тут мы получаем список который создали ранее, посредси
            exList = it
            nextExercise()
        }
        binding.bNext.setOnClickListener { // Запускаем функцию следующего упражнения
            nextExercise()
        }
    }


    private fun nextExercise() { // функция которая запускает следующее упражнение
        if (exerciseCounter < exList?.size!!) {
            timer?.cancel() // сбросили таймер, была проблема что в каждом упражнении начинался таймер после того как один раз он появился
            val ex = exList?.get(exerciseCounter++)
                ?: return // увеличиваем счётчик упражнений. через оператор элвиса нам или выдает упражнение или ретарн если его нету
            showExercise(ex)
            setExerciseType(ex)
            showNextExercise(ex)
        } else {
            exerciseCounter++ // последнее упражнение не увеличивалось. Для этого мы увеличим его вручную
            FragmentManager.setFragment(
                DaysFinishFragment.Companion.newInstance(),
                activity as AppCompatActivity
            ) // запускаем финишный фрагмент если больше нет упражнений в списке
        }

    }

    private fun showExercise(exercise: ExerciseModel?) = with(binding) {
        if (exercise == null) return@with // если нулл, то ретурн
        imMine.setImageDrawable(exercise?.image?.let {
            GifDrawable(
                root.context.assets,
                exercise.image
            )
        })
        tvName.text = exercise.name
        val title =
            "$exerciseCounter/ ${exList?.size}" // Собираем строку для акшнбара. Берем счетчик упражнений + Лист упражнений ( если он не равен налл!! (?)
        ab?.title = title // Строка которую мы собрали помещаем в Экшенбар
        ab?.setBackgroundDrawable(resources.getColor(R.color.white).toDrawable())  // Задаю фон Тулбара

    }

    private fun setExerciseType(exercise: ExerciseModel?) {
        if (exercise?.time!!.startsWith("x")) {
            timer?.cancel() // сбросим таймер если он есть
            binding.progressBar.visibility = View.INVISIBLE  // если количество повторений считаем, то прогрессбар не нужен, поэтому инвизибл
            binding.tvTime.text = exercise.time
        } else {
            binding.progressBar.visibility = View.VISIBLE // тут соответвтенно - нужен Прогрессбар
            binding.progressBar.progress =  exercise?.time!!.toInt() * 1000 // обновим максимум пб До максимума
            startTimer(exercise) // запустим таймер
        }
    }

    private fun showNextExercise(exercise: ExerciseModel?) =
        with(binding) { // функция которая запускает упражнение которое будет следом за текущим
            if (exerciseCounter < exList?.size!!) {
                val ex = exList?.get(exerciseCounter) ?: return
                imNext.setImageDrawable(GifDrawable(root.context.assets, ex.image))
                setTimeType(ex)
            } else {
                imNext.setImageDrawable(
                    GifDrawable(
                        root.context.assets,
                        "congrats.gif"
                    )
                ) // Передаём напрямую гиф с поздравлениями, если упражнений больше нет
                tvNextName.text = getString(R.string.GoodDone)
            }

        }

    private fun setTimeType(ex: ExerciseModel) {
        if (ex.time.startsWith("x")) {
            binding.tvNextName.text = ex.time
        } else {
            val name = ex.name + ": ${TimeUtils.getTime(ex.time.toLong() * 1000)}"
            binding.tvNextName.text = name
        }
    }


    private fun startTimer(exercise: ExerciseModel?) = with(binding) {

        progressBar.max =

            exercise?.time!!.toInt() * 1000  // установили максимум прогресс бара ( откуда начали отсчет. На 1000 умножили потому что в миллисекунды
        timer?.cancel() // отключили таймер чтобы не запускался предыдущий на всякий случай
        timer = object : CountDownTimer(
            exercise.time.toLong() * 1000, 1
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
        model.savePref(
            currentDay.toString(),    //// здесь было model.currentDay - потому что они сбоят с Он де тач и не ставят чек бокс по причине - фукнкция filldaysArray может сработать раньше onDetach и перезапишет её
            exerciseCounter - 1
        ) // если наступает ОнДетач ( то есть свернули приложение например) - то сохраняем текущее значение сколько выполнили упражнений в шаред преф
        timer?.cancel() // таймер выключается при выходе из приложения, иначе будут проблемы


    }


    companion object {

        @JvmStatic
        fun newInstance() = ExerciseFragment()


    }
}