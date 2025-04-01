package com.example.fitnessapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import com.example.fitnessapp.fragments.DaysFragment
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.placeholder)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        model.pref = getSharedPreferences(getString(R.string.PrefForDoing), MODE_PRIVATE)   // MODE_PRIVATE - чтобы только наше приложение могло получить доступ к таблице с данными. Название таблицы сохранили в ресурсы - String

        FragmentManager.setFragment(DaysFragment.newInstance(), this)

    }

    override fun onBackPressed() {   // узнать что сейчас актуально. Это для выхода не из приложения а на главный экран при нажатии кнопки назад на фрагменте.

        if (FragmentManager.currentFragment is DaysFragment)super.onBackPressed()
else FragmentManager.setFragment(DaysFragment.newInstance(), this)  // Теперь если основной фрагмент - экран закроется. Если не основной, то приложение не закроется, а откроется основное активити. Один фрагмент заменяет другой фрагмент если необходимо.

    }
}