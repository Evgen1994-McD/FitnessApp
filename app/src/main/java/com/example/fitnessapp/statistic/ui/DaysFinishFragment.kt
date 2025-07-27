package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnessapp.databinding.FinishBinding
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DaysFinishFragment : Fragment() {
    private lateinit var binding: FinishBinding
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений
    private val model: DaysFinishViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FinishBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.Done)
        observerCurrentDayStatisitcs()

        model.getStatisticByDate(TimeUtils.getCurrentDate())


        binding.bBack.setOnClickListener {
            findNavController()
                .popBackStack(
                    R.id.trainingFragment,
                    inclusive = false
                )

        }

    }


    private fun observerCurrentDayStatisitcs(){
        model.statisticData.observe(viewLifecycleOwner){ statisitc->
            binding.tvKcal.text = statisitc.kcal.toString()
            binding.tvTime.text = statisitc.workoutTime
            binding.tvExCounter.text = arguments?.getString("tec") ?: "0"
        }
    }


    companion object {

        @JvmStatic
        fun newInstance() = DaysFinishFragment()


    }
}