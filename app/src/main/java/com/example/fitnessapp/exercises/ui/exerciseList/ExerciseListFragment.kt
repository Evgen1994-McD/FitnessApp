package com.example.fitnessapp.exercises.ui.exerciseList

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.exercises.ui.adapters.ExerciseAdapter
import com.example.fitnessapp.exercises.ui.compose.ExerciseBottomSheet
import com.example.fitnessapp.exercises.ui.exercise.ExerciseViewModel
import com.example.fitnessapp.statistic.ui.DaysFinishViewModel
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.getDayFromArguments
import kotlinx.coroutines.delay
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import kotlinx.coroutines.launch

class ExerciseListFragment : Fragment() {
    private var dayModel: DayModel? = null
    private lateinit var binding: ExerciseListFragmentBinding
    private lateinit var adapter: ExerciseAdapter
    private val model: ExerciseListViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений
    private lateinit var sharedPreferences: SharedPreferences
    private var isBottomSheetShowing = false // Флаг для дебаунса

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = ExerciseListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Проверяем что передано - day_id или готовый DayModel
        dayModel = getDayFromArguments() 
        if (dayModel == null) {
            // Если dayModel не передан, пробуем получить day_id
            val dayId = arguments?.getInt("day_id") ?: -1
            if (dayId != -1) {
                // Загружаем DayModel по day_id через ViewModel
                model.getDayById(dayId).observe(viewLifecycleOwner) { day ->
                    dayModel = day
                    // Обновляем заголовок с правильным номером дня
                    ab?.title = ("День: ${day?.dayNumber ?: "?"}. Список упражнений.")
                    day?.let {
                        model.getDayExerciseList(it)
                    }
                }
            }
        }

        // Инициализируем SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("exercise_prefs", Context.MODE_PRIVATE)

        init() //функия инит которая ниже
        exerciseListObserver()
        topCardObserver() // запускаю топКардОбсервер который будет мне обновлять состояние вью
        selectedExerciseObserver() // добавляю observer для выбранного упражнения
/*
Функция getDayFromArguments - это экстеншен функция, которыую мы создали чтобы использовать
на любом фрагменте.
Вызввается она просто импортом - то есть вызываешь её как будто она уже есть в классе,
а затем импортируешь
 */

        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = ("День: ${dayModel?.dayNumber ?: "?"}. Список упражнений.")

    }

    private fun init() = with(binding) {  // Инициализируем Адаптер и добавляем RecyclerVIew
        adapter = ExerciseAdapter(
            onExerciseClick = { exercise ->
                // Обработка клика на упражнение
                model.getExerciseById(exercise.id ?: 0)
            },
            onInfoClick = { exercise ->
                // Обработка клика на иконку информации
                showExerciseBottomSheet(exercise)
            }
        )
        rcView.layoutManager = LinearLayoutManager(activity)
        rcView.adapter = adapter // Назначили адаптер
        
        // Загружаем список упражнений при инициализации
        dayModel?.let { day ->
            model.getDayExerciseList(day)
        }
        
        bStart.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("day", dayModel )
            }
            findNavController().navigate(R.id.action_exListFragment_to_exerciseFragment, bundle)
        }
        
        // Обработчик клика на FAB для вызова afterTrainingDialog
        fabEdit.setOnClickListener {
            dayModel?.let { day ->
                val finishViewModel = activityViewModels<DaysFinishViewModel>().value
                DialogManager.showAfterTrainingDialog(
                    requireContext(),
                    object : DialogManager.OnDifficultySelectedListener {
                        override fun onDifficultySelected(difficultyLevel: Int, zone: String?) {
                            if (difficultyLevel == 1) {
                                finishViewModel?.addTrainingHarder(day.difficulty, day.zone)
                            } else if (difficultyLevel == 2) {
                                finishViewModel?.reduceTrainingComplexity(day.difficulty, day.zone)
                            }
                            // Получаем свежий dayModel из базы и обновляем список
                            viewLifecycleOwner.lifecycleScope.launch {
                                delay(2500) // ждем 2.5 секунды как в диалоге
                                dayModel = getDayFromArguments() // получаем обновленный dayModel с новыми ID
                                model.getDayExerciseList(dayModel)
                            }
                        }
                    },
                    day.zone
                )
            }
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
showInstructionToast() // Показываем Toast с инструкцией
}
    }

    private fun showInstructionToast() {
        val toastCount = sharedPreferences.getInt("instruction_toast_count", 0)
        
        if (toastCount < 3) {
            Toast.makeText(
                requireContext(),
                "Нажмите на упражнение чтобы посмотреть инструкцию",
                Toast.LENGTH_LONG
            ).show()
            
            // Увеличиваем счетчик показов
            sharedPreferences.edit()
                .putInt("instruction_toast_count", toastCount + 1)
                .apply()
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
getString(R.string.statistic)
} else tvRestText
                tvRestDays.visibility = View.VISIBLE
                tvRestDays.startAnimation(alphaAnimationText2)
progressbar.max = card.maxProgress * 100

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


    private fun selectedExerciseObserver() {
        model.selectedExercise.observe(viewLifecycleOwner) { exercise ->
            exercise?.let {
                showExerciseBottomSheet(it)
            }
        }
    }

    private fun showExerciseBottomSheet(exercise: com.example.fitnessapp.db.ExerciseModel) {
        // Проверяем флаг дебаунса
        if (isBottomSheetShowing) {
            return
        }
        
        isBottomSheetShowing = true
        
        val composeView = ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setContent {
                FitnessAppTheme () {
                    ExerciseBottomSheet(
                        exercise = exercise,
                        onDismiss = {
                            // Сбрасываем флаг при закрытии
                            isBottomSheetShowing = false
                            // Очищаем selectedExercise при закрытии
                            model.selectedExercise.value = null
                            // Удаляем ComposeView из parent
                            (parent as? ViewGroup)?.removeView(this)
                        }
                    )
                }
            }
        }
        
        // Добавляем ComposeView в корневой layout
        binding.root.addView(composeView)
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