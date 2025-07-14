package com.example.fitnessapp.customTraining.daysList.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.customTraining.adapter.CustomDaysAdapter
import com.example.fitnessapp.databinding.FragmentCustomDaysListBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.utils.DialogManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomDaysListFragment : Fragment(), CustomDaysAdapter.Listener {
    private lateinit var daysAdapter: CustomDaysAdapter

        private var _binding: FragmentCustomDaysListBinding? = null //ЭТО сам байндинг Налл
        private val binding get() = _binding!! // а здесь мы получаем байндинг
    private val model: CustomDaysListViewModel by viewModels()


        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            _binding = FragmentCustomDaysListBinding.inflate(
                inflater,
                container,
                false
            )
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.addNewDayButton.setOnClickListener {
                model.insertDay(
                    DayModel(null,
                        "",
                        "custom",
                        false,
                        0,
                        0)
                    /*
                    При нажатии на кнопку "Создать день создаём день.
                    Но не заполняем его упражнениями, это будем делать позже
                     */
                )
            }

            daysListObserver()
            initRcView()

        }

    private fun initRcView(){
        binding.apply {
            rcView.layoutManager = LinearLayoutManager(requireContext())
            daysAdapter = CustomDaysAdapter(this@CustomDaysListFragment)
            rcView.adapter = daysAdapter
        }
    }

    private fun daysListObserver(){
        model.daysListData.observe(viewLifecycleOwner){ list ->
            daysAdapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

    override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

    override fun onClick(day: DayModel) {
     findNavController().navigate(R.id.selectedExerciseListFragment)
    }

    override fun onDelete(day: DayModel) {
DialogManager.showDialog(requireContext(), R.string.delete_day, object :DialogManager.Listener{
    override fun onClick() {
      model.deleteDay(day)
    }
/*
Удаление добавленных дней
Добавили иконку
Добавили строку, вызываем диалог при нажантии
Если пользователь хочет удалить, с помощью вью модел( написали там функцию)
- Удалаяем
 */
})

    }

    /*
    В onDestroyView наш байндинг приравниваем обратно к null
    Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
    но доступ к байдингу всё ещё есть
     */

    }


