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

@AndroidEntryPoint
class ExerciseFragment : Fragment() {
    private lateinit var binding: ExerciseBinding
    private val model: ExerciseViewModel by viewModels()
    private var totalExerciseCounter = "0"

    private var currentDay: DayModel? = null
    private var ab: ActionBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ab = (activity as AppCompatActivity).supportActionBar

        // Инициализируем XML кнопку
        binding.bAddTime?.visibility = View.INVISIBLE

        currentDay = getDayFromArguments()
        updateExercise()
        updateTime()
        updateToolbar()
        currentDay?.let { model.getExercises(it) }

        binding.lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        binding.bNext.setOnClickListener {
            if (binding.bNext.text.toString() == getString(R.string.statistic)) {
                var bundle = Bundle()
                bundle.putString("tec", totalExerciseCounter)
                bundle.putString("difficulty", "${currentDay?.difficulty}")
                bundle.putString("zone", "${currentDay?.zone}")
                findNavController().navigate(R.id.action_exerciseFragment_to_daysFinishFragment, bundle)
            } else {
                model.nextExercise()
            }
        }

        // Обработчик для кнопки +20 сек
        binding.bAddTime?.setOnClickListener {
            val isRest = binding.subTitle.text.toString().startsWith(getString(R.string.relax))
            if (isRest) {
                // Обновляем max значение прогресс бара
                val currentMax = binding.progressBar.max
                binding.progressBar.max = (currentMax + 20000).toInt()
                
                // Обновляем таймер в ViewModel - передаем время в секундах
                model.currentTimerValue?.let { currentTime ->
                    val newTimeInSeconds = (currentTime + 20000L) / 1000
                    android.util.Log.d("ExerciseFragment", "Updating timer: $currentTime ms -> $newTimeInSeconds s")
                    model.updateTimerValue(newTimeInSeconds)
                }
            }
        }
    }

    private fun updateExercise() = with(binding) {
        model.updateExercise.observe(viewLifecycleOwner) { exercise ->
            imMine.setImageDrawable(exercise?.image?.let {
                GifDrawable(root.context.assets, exercise.image)
            })

            tvName.text = exercise.name
            subTitle.text = exercise.subtitle
            
            val isRest = subTitle.text.toString().startsWith(getString(R.string.relax))
            setMainColors(!isRest)
            setPreFinishColors(subTitle.text.toString().startsWith(getString(R.string.day_finish_subtitle)))
            
            changeButtonText(exercise.name)
            showTime(exercise)
        }
    }

    private fun changeButtonText(title: String) {
        if (title == getString(R.string.day_finish_name)) {
            binding.bNext.text = getString(R.string.statistic)
        }
    }

    private fun updateTime() = with(binding) {
        model.updateTime.observe(viewLifecycleOwner) { time ->
            android.util.Log.d("ExerciseFragment", "Timer time: $time ms")
            tvTime.text = TimeUtils.getTime(time)
            animProgressBar(time)
        }
    }

    private fun updateToolbar() {
        model.updateToolbar.observe(viewLifecycleOwner) { text ->
            ab?.title = text
            totalExerciseCounter = text.split("/")[1]
        }
    }

    private fun showTime(exercise: ExerciseModel?) {
        if (exercise?.time!!.startsWith("x") || exercise.time.isEmpty()) {
            binding.progressBar.visibility = View.INVISIBLE
            binding.tvTime.text = exercise.time
            binding.bAddTime?.visibility = View.INVISIBLE
        } else {
            binding.progressBar.visibility = View.VISIBLE
            val totalTime = exercise.time.toLong() * 1000 // Базовое время в миллисекундах
            binding.progressBar.max = totalTime.toInt()
            binding.progressBar.progress = totalTime.toInt()
            model.startTimer(exercise.time.toLong())
        }
    }

    private fun setMainColors(isExercise: Boolean) = with(binding) {
        val background = ContextCompat.getColor(requireContext(), R.color.background)
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val blue = ContextCompat.getColor(requireContext(), R.color.blue)
        val blueDark = ContextCompat.getColor(requireContext(), R.color.blue_dark)
        val black = ContextCompat.getColor(requireContext(), R.color.black)

        android.util.Log.d("ExerciseFragment", "setMainColors called: isExercise=$isExercise")
        android.util.Log.d("ExerciseFragment", "subTitle text: '${subTitle.text}'")
        android.util.Log.d("ExerciseFragment", "relax string: '${getString(R.string.relax)}'")
        android.util.Log.d("ExerciseFragment", "addTimeButton found: ${bAddTime != null}")

        if (isExercise) {
            bg.setBackgroundColor(background)
            tvName.setTextColor(textColor)
            subTitle.setTextColor(textColor)
            tvTime.setTextColor(textColor)
            progressBar.progressTintList = ColorStateList.valueOf(blueDark)
            progressBar.backgroundTintList = ColorStateList.valueOf(white)
            bNext.backgroundTintList = ColorStateList.valueOf(blue)
            bNext.setTextColor(white)
            bAddTime?.visibility = View.INVISIBLE
            android.util.Log.d("ExerciseFragment", "Button set to GONE during exercise")
        } else {
            bg.setBackgroundColor(blue)
            tvName.setTextColor(white)
            subTitle.setTextColor(white)
            tvTime.setTextColor(white)
            progressBar.progressTintList = ColorStateList.valueOf(white)
            progressBar.backgroundTintList = ColorStateList.valueOf(white)
            bNext.backgroundTintList = ColorStateList.valueOf(white)
            bNext.setTextColor(black)
            binding.bAddTime?.visibility = View.VISIBLE
            android.util.Log.d("ExerciseFragment", "Button set to VISIBLE during rest")
            android.util.Log.d("ExerciseFragment", "Final button visibility: ${binding.bAddTime?.visibility}")
        }
    }

    private fun setPreFinishColors(isExercise: Boolean) = with(binding) {
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val blue = ContextCompat.getColor(requireContext(), R.color.blue)
        val blueDark = ContextCompat.getColor(requireContext(), R.color.blue_dark)
        val black = ContextCompat.getColor(requireContext(), R.color.black)

        if (isExercise) {
            bg.setBackgroundColor(white)
            imMine.visibility = View.INVISIBLE
            lottieView.visibility = View.VISIBLE
            lottieView.playAnimation()
            tvName.setTextColor(black)
            subTitle.setTextColor(blueDark)
            tvTime.setTextColor(black)
            bNext.backgroundTintList = ColorStateList.valueOf(blue)
            bNext.setTextColor(white)
            bAddTime?.visibility = View.INVISIBLE
        }
    }

    private fun animProgressBar(restTime: Long) {
        val progressTo = if (restTime > 1000) {
            restTime - 1000
        } else {
            0
        }

        val anim = ObjectAnimator.ofInt(
            binding.progressBar,
            "progress",
            binding.progressBar.progress,
            progressTo.toInt()
        )
        anim.duration = 700
        anim.start()
    }

    override fun onPause() {
        super.onPause()
        model.onPause()
    }
}
