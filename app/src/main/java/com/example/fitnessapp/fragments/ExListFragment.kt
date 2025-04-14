package com.example.fitnessapp.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.ExerciseAdapter
import com.example.fitnessapp.databinding.ExerciseListFragmentBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.MainViewModel

class ExListFragment : Fragment() {
    private var dayModel: DayModel? = null
    private lateinit var binding: ExerciseListFragmentBinding
    private lateinit var adapter: ExerciseAdapter
    private val model: MainViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
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
       Log.d("MyLog", "Exercises:${dayModel?.exercises}")
        init() //функия инит которая ниже
        model.mutableListExercise.observe(viewLifecycleOwner) {  // Тут мы получаем список который создали ранее, посредси
            for (i in 0 until model.getExerciseCount()) { // это мы делаем чтобы отметить чекбоксы. Берем с позиции 0, и отмечаем до позиции выполнненных упражнений в определенном дне
                it[i] =
                    it[i].copy(isDone = true) // это i = i скопировать статус isDone = true и поставить галочки.
            }  // то есть берем элемент и если это упражнение выполнено, то перезеписываем переменнную isDone = true. Вот и всё. По умолчанию она как бы False !
            adapter.submitList(it)
        }
    }

    private fun getDayFromArguments(): DayModel? {
        return arguments.let { bundle ->
            if (Build.VERSION.SDK_INT >= 33) {
                bundle?.getSerializable("day", DayModel::class.java)

            } else {
                bundle?.getSerializable("day") as DayModel
            }

        }
    }


    private fun init() = with(binding) {  // Инициализируем Адаптер и добавляем RecyclerVIew
        adapter = ExerciseAdapter()
        rcView.layoutManager = LinearLayoutManager(activity)
        rcView.adapter = adapter // Назначили адаптер
        bStart.setOnClickListener {
            FragmentManager.setFragment(
                WaitingFragment.newInstance(),
                activity as AppCompatActivity
            )  // открываем фрагмент с помощью кнопки начать
        }

    }


    companion object {

        @JvmStatic
        fun newInstance() = ExListFragment()


    }
}