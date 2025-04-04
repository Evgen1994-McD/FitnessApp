package com.example.fitnessapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnessapp.databinding.FinishBinding
import com.example.fitnessapp.utils.FragmentManager
import pl.droidsonroids.gif.GifDrawable
import androidx.appcompat.app.ActionBar
import com.example.fitnessapp.R
import com.example.fitnessapp.training.ui.fragments.DaysFragment


class DaysFinishFragment : Fragment() {
    private lateinit var binding: FinishBinding
    private var ab: ActionBar? = null // добавили переменную для ActionBar, будем показывать счетчик упражнений

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FinishBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.Done)
       binding.imFinish.setImageDrawable(GifDrawable((activity as AppCompatActivity).assets, "congrats.gif")) // передали ассетс через активити аз апкомпакт активити
    binding.bBack.setOnClickListener {
        FragmentManager.setFragment(DaysFragment.newInstance(), activity as AppCompatActivity) // перебросим на активити сразу при нажатии на кнопку
    }

    }







    companion object {

        @JvmStatic
        fun newInstance() = DaysFinishFragment()


    }
}