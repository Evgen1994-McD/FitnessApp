package com.example.fitnessapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DayModel
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.databinding.FragmentDaysBinding
import java.util.zip.Inflater

class DaysFragment : Fragment() {
    private lateinit var binding: FragmentDaysBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
   initRcView()
    }


    private fun initRcView() = with(binding){
        val adapter = DaysAdapter()
        rcviewdays.layoutManager = LinearLayoutManager(activity as AppCompatActivity)
        rcviewdays.adapter = adapter
        adapter.submitList(fillDaysArray())
    }


    private fun fillDaysArray() : ArrayList<DayModel>{
        val tArray = ArrayList<DayModel>() // создаём класс для заполнения из массивов с упражнениями. получается массив с упражнениями который состоит из DayModel
    resources.getStringArray(R.array.day_exercise).forEach {
    tArray.add(DayModel(it, false))
    }
    return tArray
    }

    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()


    }
}