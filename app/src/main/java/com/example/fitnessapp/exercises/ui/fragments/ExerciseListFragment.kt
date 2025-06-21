package com.example.fitnessapp.exercises.ui.fragments

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.ExerciseAdapter
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.ui.ExerciseListViewModel
import com.example.fitnessapp.fragments.WaitingFragment
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.getDayFromArguments

class ExerciseListFragment : Fragment() {
    private var dayModel: DayModel? = null
    private lateinit var binding: ExerciseListFragmentBinding
    private lateinit var adapter: ExerciseAdapter
    private val model: ExerciseListViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = ExerciseListFragmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.listExercise)
        ab?.setBackgroundDrawable(
            resources.getColor(R.color.white).toDrawable()
        )  // Задаю фон Тулбара

        super.onViewCreated(view, savedInstanceState)
        dayModel = getDayFromArguments() // Вызвавли функци получания Деймодел и открытия нужного объекта

        init() //функия инит которая ниже
        exerciseListObserver()
        dayModel = getDayFromArguments()
        model.getDayExerciseList(dayModel)
        topCardObserver() // запускаю топКардОбсервер который будет мне обновлять состояние вью
/*
Функция getDayFromArguments - это экстеншен функция, которыую мы создали чтобы использовать
на любом фрагменте.
Вызввается она просто импортом - то есть вызываешь её как будто она уже есть в классе,
а затем импортируешь
 */
    }




    private fun init() = with(binding) {  // Инициализируем Адаптер и добавляем RecyclerVIew
        adapter = ExerciseAdapter()
        rcView.layoutManager = LinearLayoutManager(activity)
        rcView.adapter = adapter // Назначили адаптер
        bStart.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("day", dayModel )
            }
findNavController().navigate(R.id.action_exListFragment_to_exerciseFragment,
    bundle)
        }
        /*
        Прикольно.
        Переход на фрагмент с упражнениями сделали с помощью навигации, а чтобы передать туда
        экземпляр класса Daymodel - создали бандл и положили туда этот деймодел, который ранее получили с другого экрана
        (с Training Fragment ( который у меня домашний фрагмент))
         */

    }
    private fun exerciseListObserver(){ // делаю эксерсайз лист обсервер и здесь мы используем класс вью модел
model.exerciseList.observe(viewLifecycleOwner) { list -> // этот обсервер выдаёт лист как только он появится. Этот лист надо будет передавать в наш адаптер
adapter.submitList(list) // передали этот список
}
    }
    private fun topCardObserver(){
        model.topCardUpdate.observe(viewLifecycleOwner){ card ->
            binding.apply {
                val alphaAnimation = AlphaAnimation(0.2f, 1.0f)
                alphaAnimation.duration = 700
                im.setImageResource(card.imageId)
                im.startAnimation(alphaAnimation)

                val alphaAnimationText = AlphaAnimation(0.0f, 1.0f)
                alphaAnimationText.startOffset = 300  // Задержка запуска анимации чтобы не сразу запускать
                alphaAnimationText.duration = 800
                difTitle.setText(card.difficultyTitle)
                difTitle.visibility = View.VISIBLE
                difTitle.startAnimation(alphaAnimationText)


                val alphaAnimationText2 = AlphaAnimation(0.0f, 1.0f)
                alphaAnimationText2.startOffset = 600  // Задержка запуска анимации чтобы не сразу запускать
                alphaAnimationText2.duration = 800
                val daysRest = card.maxProgress - card.progress
val tvRestText = getString(R.string.rest) + " " + daysRest
tvRestDays.text = if(daysRest == 0) {
getString(R.string.Done)
} else tvRestText
                tvRestDays.visibility = View.VISIBLE
                tvRestDays.startAnimation(alphaAnimationText2)
progressbar.max = card.maxProgress

                animProgressBar(card.progress)
                    /*
                    Сделали функцию топ кард обсервер. Она подписывается на обновление лайв дата и обновляет
                    состояние экрана при получении изменений
                    alphaAnimation - картинка
                    alphaAnimationText - сложность
                    alphaAnimationText2 - осталось дней

                     */
            }


        }
    }


    private fun animProgressBar ( progress : Int) {
        val anim = ObjectAnimator.ofInt(
            binding.progressbar,
            "progress",
            binding.progressbar.progress,
            progress * 100
        )
        anim.startDelay = 900 // Задержка появления прогресс бара
        anim.duration = 700
        anim.start()

        /*
        анимация прогресс бара ( сколько дней сделано)
        умножается на 100 чтобы не было рывков
        duration - за сколько милисекунд дойдём до целевого прогресса
         */
    }


}