package com.example.fitnessapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessapp.fragments.DaysFragment
import com.example.fitnessapp.utils.FragmentManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.placeholder)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FragmentManager.setFragment(DaysFragment.newInstance(), this)
    }

    override fun onBackPressed() {   // узнать что сейчас актуально. Это для выхода не из приложения а на главный экран при нажатии кнопки назад на фрагменте.

        if (FragmentManager.currentFragment is DaysFragment)super.onBackPressed()
else FragmentManager.setFragment(DaysFragment.newInstance(), this)  // Теперь если основной фрагмент - экран закроется. Если не основной, то приложение не закроется, а откроется основное активити. Один фрагмент заменяет другой фрагмент если необходимо.

    }
}