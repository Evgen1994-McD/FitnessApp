package com.fit.fitnessapp.exercises.ui.training

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fit.fitnessapp.R
import com.fit.fitnessapp.databinding.FragmentTrainingBinding
import com.fit.fitnessapp.exercises.ui.days.DaysFragment
import com.fit.fitnessapp.exercises.ui.days.DaysViewModel
import com.fit.fitnessapp.exercises.utils.TrainingUtils

class TrainingFragment : Fragment() {

private lateinit var binding: FragmentTrainingBinding
    private val model: DaysViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Получаем сложность и зону из аргументов
        val difficulty = arguments?.getString("difficulty") ?: TrainingUtils.EASY
        val zone = arguments?.getString("zone")
        
        // Показываем bt_custom_edit только для custom сложности
        binding.btCustomEdit.visibility = if (difficulty == TrainingUtils.CUSTOM) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        // Добавляем обработчик нажатия на bt_custom_edit
        binding.btCustomEdit.setOnClickListener {
            findNavController().navigate(R.id.customDaysListFragment)
        }
        
        // Находим соответствующий TrainingTopCardModel
        val topCardModel = TrainingUtils.topCardList.find { it.difficulty == difficulty }
            ?: TrainingUtils.topCardList[0]
        
        // Загружаем дни для выбранной сложности и зоны
        model.getExerciseDaysByDifficulty(topCardModel, zone)
        
        // Показываем DaysFragment вместо ViewPager
        showDaysFragment()
        
        topCardObserver()
    }

    private fun topCardObserver() = with(binding){
        model.topCardUpdate.observe(viewLifecycleOwner){ card ->
            val alphaAnimation = AlphaAnimation(0.2f, 1.0f)
            alphaAnimation.duration = 700
            im.setImageResource(card.imageId)
            
            // Если есть текстовый заголовок (для зон), используем его, иначе - ресурс сложности
            if (card.title.isNotEmpty()) {
                difTitle.text = card.title
            } else {
                difTitle.setText(card.difficultyTitle)
            }
            
            progressbar.max = card.maxProgress * 100
            val restDays = card.maxProgress - card.progress
            animProgressBar(card.progress)
            val daysRestText = getString(R.string.rest) + " " + restDays
            tvRestDays.text = daysRestText
        }
    }
/* Сделали прогресс, передачу сколько осталось дней + текст
Передаём с помощью обсервера ( вью модел)!
 */
private fun animProgressBar ( progress : Int) {
    val anim = ObjectAnimator.ofInt(
        binding.progressbar,
        "progress",
        binding.progressbar.progress,
        progress * 100
    )
    anim.duration = 700
    anim.start()

    /*
    анимация прогресс бара ( сколько дней сделано)
    умножается на 100 чтобы не было рывков
    duration - за сколько милисекунд дойдём до целевого прогресса
     */
}

    private fun showDaysFragment() {
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        
        val daysFragment = DaysFragment.newInstance()
        fragmentTransaction.replace(R.id.daysFragmentContainer, daysFragment)
        fragmentTransaction.commit()
    }

    }