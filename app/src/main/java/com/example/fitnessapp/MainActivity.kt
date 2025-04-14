package com.example.fitnessapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.example.fitnessapp.training.ui.fragments.DaysFragment
import com.example.fitnessapp.training.ui.fragments.TrainingFragment
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
@AndroidEntryPoint  // Это точка входа для DaggerHilt, указать если нужно получать инстанции для Хилт
class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)




    }

    }
