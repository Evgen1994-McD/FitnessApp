package com.example.fitnessapp.customTraining.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.databinding.FragmentCustomDaysListBinding
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.utils.DialogManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomDaysListFragment : Fragment(), DaysAdapter.Listener {
    private lateinit var daysAdapter: DaysAdapter

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
            daysAdapter = DaysAdapter(this@CustomDaysListFragment)
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
        TODO("Not yet implemented")
    }

    /*
    В onDestroyView наш байндинг приравниваем обратно к null
    Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
    но доступ к байдингу всё ещё есть
     */

    }


