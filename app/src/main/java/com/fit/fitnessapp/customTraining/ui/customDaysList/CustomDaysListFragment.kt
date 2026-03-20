package com.fit.fitnessapp.customTraining.ui.customDaysList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fit.fitnessapp.R
import com.fit.fitnessapp.databinding.FragmentCustomDaysListBinding
import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.dao.ExerciseDao
import com.fit.fitnessapp.exercises.utils.TrainingUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomDaysListFragment : Fragment(), CustomDaysAdapter.Listener {
    private lateinit var daysAdapter: CustomDaysAdapter

    @Inject
    lateinit var exerciseDao: ExerciseDao

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
            (requireActivity() as AppCompatActivity).
                supportActionBar?.title = getString(R.string.custom_training_title_ab)
            
            // Скрываем навигацию назад но оставляем bottom меню
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(false)

            binding.addNewDayButton.setOnClickListener {
                // Проверяем, что кнопка активна
                if (binding.addNewDayButton.isEnabled) {
                    model.insertDay(
                        DayModel(null,
                            "",
                            getString(R.string.custom),
                            false,
                            0,
                            0,
                            true, // isOpen = true для кастомных тренировок
                            zone = null,
                            completedDate = null)
                        /*
                        При нажатии на кнопку "Создать день создаём день.
                        Но не заполняем его упражнениями, это будем делать позже
                         */
                    )
                } else {
                    // Логирование для отладки
                    android.util.Log.d("CustomDays", "Кнопка неактивна")
                }
            }
            
            // Добавляем обработчик нажатия на bt_start
            binding.btStart.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("difficulty", TrainingUtils.CUSTOM)
                }
                findNavController().navigate(R.id.trainingListFragment, bundle)
            }

            daysListObserver()
            initRcView()
            
            // Устанавливаем начальное состояние загрузки
            model.setLoadingState(true)
            
            // Принудительно делаем кнопку активной и добавляем отладку
            binding.addNewDayButton.isEnabled = true
            android.util.Log.d("CustomDays", "Кнопка isEnabled: ${binding.addNewDayButton.isEnabled}")
            android.util.Log.d("CustomDays", "Кнопка visibility: ${binding.addNewDayButton.visibility}")

        }

    private fun initRcView(){
        binding.apply {
            rcView.layoutManager = LinearLayoutManager(requireContext())
            daysAdapter = CustomDaysAdapter(this@CustomDaysListFragment, exerciseDao)
            rcView.adapter = daysAdapter
            createItemTouchHelper().attachToRecyclerView(rcView)
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, 
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Не разрешаем перемещение
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val currentList = daysAdapter.currentList
                if (position >= 0 && position < currentList.size) {
                    val day = currentList[position]
                    model.deleteDay(day)
                }
            }
        })
    }

    private fun daysListObserver(){
        model.daysListData.observe(viewLifecycleOwner){ list ->
            // Останавливаем загрузку когда получены данные
            model.setLoadingState(false)
            
            val isEmpty = list.isEmpty()
            
            // Управляем видимостью плейсхолдера и текстов
            binding.textEmpty.visibility = if(isEmpty) View.VISIBLE else View.GONE
            binding.imageEmpty.visibility = if(isEmpty) View.VISIBLE else View.GONE
            binding.textEmptySubtext.visibility = if(isEmpty) View.VISIBLE else View.GONE
            
            // Управляем видимостью кнопки bt_start
            binding.btStart.visibility = if(isEmpty) View.GONE else View.VISIBLE
            
            daysAdapter.submitList(list)
        }
        
        model.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            // Во время загрузки скрываем плейсхолдер и кнопку
            if (isLoading) {
                binding.textEmpty.visibility = View.GONE
                binding.imageEmpty.visibility = View.GONE
                binding.textEmptySubtext.visibility = View.GONE
                binding.btStart.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(day: DayModel) {
        val bundle = Bundle().apply {
            putInt("day_id", day.id ?: -1)
            putInt("day_number", day.dayNumber)
        }
     findNavController().navigate(R.id.selectedExerciseListFragment, bundle)
    }

    /*
    В onDestroyView наш байндинг приравниваем обратно к null
    Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
    но доступ к байдингу всё ещё есть
     */

    }


