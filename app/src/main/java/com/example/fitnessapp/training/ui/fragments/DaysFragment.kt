package com.example.fitnessapp.training.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.databinding.FragmentDaysBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.training.ui.DaysViewModel
import com.example.fitnessapp.utils.DialogManager

@Suppress("DEPRECATION")
class DaysFragment : Fragment(), DaysAdapter.Listener { // Подключили интерфейс из который создали в DaysAdapter
    private lateinit var adapter : DaysAdapter  // мы вынесли адаптер сюда, чтобы ОБНОВИТЬ шаред префс по факту как только пользователь нажал очистить
    private lateinit var binding: FragmentDaysBinding
    private val model: DaysViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

   initRcView()
    }







    private fun initRcView() = with(binding){
        adapter = DaysAdapter(this@DaysFragment)
        rcviewdays.layoutManager = LinearLayoutManager(activity as AppCompatActivity)
        rcviewdays.adapter = adapter
        rcviewdays.itemAnimator = null

    }

    private fun updateAdapter() {
        model.daysList.observe(viewLifecycleOwner) { list -> /* обсервер
        с помощью него передаём и обновляем Лист который получим в адаптере */

            adapter.submitList(list) //тут временный эмпти лист
        }

    }

    override fun onResume() {
        super.onResume()
        updateAdapter() // ToDo будем запускать отсюда апдейт адаптер
    }





  



    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()

        } // мы настроили newInstance - передаём туда сложность




    override fun onClick(day: DayModel) {  // функция интерфейса  //Фунцкия  перехода на фрагмент с упражнениями
        if (day.isDone) {

            DialogManager.showDialog(
                activity as AppCompatActivity,
                R.string.reset_day_message,
                object : DialogManager.Listener{
                    override fun onClick() {
                        model.resetSelectedDay(day)
                        openExerciseListFragment(day)
                    } // с помощью Диалог менеджера сделал стирание только определенного дня, а не всех сразу. Получается, переиспользование кода выше, только с заменой ресурса
// мы передаем количество выполненных упражнений в 0, поэтому день обнуляется при нажатии на диалог
                })


        }else {
            openExerciseListFragment(day)


        }
    }

   private fun openExerciseListFragment(day: DayModel){
        val bundle = Bundle().apply {
            putSerializable("day", day) // Делаем Бандл. Чтобы передать целвй класс нужно делать
            // через сериализацию
        }

        findNavController().navigate(R.id.action_trainingFragment_to_exListFragment, bundle)
    }


}
