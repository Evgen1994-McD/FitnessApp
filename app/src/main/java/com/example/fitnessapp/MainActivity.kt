package com.example.fitnessapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.fitnessapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import kotlin.getValue
@AndroidEntryPoint  // Это точка входа для DaggerHilt, указать если нужно получать инстанции для Хилт
class MainActivity : AppCompatActivity() {
@Inject
lateinit var tts:TextToSpeech // инициализируем в MainActivity потому что это долгая операция, будем держать в памяти
    private val model: MainViewModel by viewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController : NavController
    private lateinit var bottomNavigationView:BottomNavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        val toolbar = binding.materialToolbar
        setSupportActionBar(toolbar)
model.controlTheme()




        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController =  navHostFragment.navController
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.customDaysListFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                }
                R.id.selectedExerciseListFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                }
                R.id.chooseExercisesFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                }
                R.id.exListFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                } R.id.exerciseFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                } R.id.daysFinishFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }



/*
мы можем указывать нижнее подчеркивание, если не используем некоторые переменные методов, например
 */

    }



}
