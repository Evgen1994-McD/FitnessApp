package com.example.fitnessapp.training.ui.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentTrainingBinding
import com.example.fitnessapp.training.ui.DaysViewModel
import com.example.fitnessapp.training.ui.adapters.VpAdapter
import com.example.fitnessapp.training.utils.TrainingUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.getValue

class TrainingFragment : Fragment() {
    private val diffList = listOf(
        "easy",
        "middle",
        "hard"
    )

private lateinit var binding: FragmentTrainingBinding
    private val model: DaysViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTrainingBinding.inflate(inflater, container, false) //ТУТ ХЗ что, точно не понял
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topCardObserver()
        isCustomTrainingEmpty()
        model.getCustomDaysList()

    }
    private fun isCustomTrainingEmpty(){
        model.isCustomListEmpty.observe(viewLifecycleOwner){
            val index = if(it){
                1
            } else {
                0
            }
            initVpAdapter(index)

        }
    }

    private fun topCardObserver() = with(binding){
        model.topCardUpdate.observe(viewLifecycleOwner){ card ->
            val alphaAnimation = AlphaAnimation(0.2f, 1.0f)
            alphaAnimation.duration = 700
im.setImageResource(card.imageId)
difTitle.setText(card.difficultyTitle)
progressbar.max =  card.maxProgress * 100
val restDays = card.maxProgress - card.progress
            animProgressBar( card.progress)
            val daysRestText = getString(R.string.rest) +" "+ restDays
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

    private fun initVpAdapter(index: Int){
        val vpAdapter = VpAdapter(this, index) // Мутим метод онВьюкреатед и вызываем тут наш адаптер ВПадаптер
        binding.vp.adapter = vpAdapter //привязываем адаптер к ВьюПейджеру. Но мы хотим связать Пейджер и ТабЛайоут
        //Для связки нужен специальный Медиатор
        var ab =
            (activity as AppCompatActivity).supportActionBar // Инициализировали экшнбар в он вью креатед

        ab?.title = "Список тренировок"    // Хардкод потом перепишу
        TabLayoutMediator(binding.tabLayout, binding.vp){ tab, pos ->  // Для применения указать таблайоут + ВьюПейджер
//мы хотим менять названия у наших табов, сейчас этим и займемся
            tab.text = getString(TrainingUtils.tabTitles[pos]) //Передаём в табы текст из ресурсов по позиции
        }.attach() // обязательно делается Аттач, иначе не будет работать
        binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){ // хотим знать какой фрагмент во вью пейджере
            //мы открыли
            override fun onPageSelected(position: Int) { // выбрали что будем знать фрагмент. тут передаётся 0, 1 или 2 позиция
                // в соответствие с позицией уже открывается изи мидл или хард
                super.onPageSelected(position)
                model.getExerciseDaysByDifficulty(
                    TrainingUtils.topCardList[position]
                )
            }
        })
    }

    }