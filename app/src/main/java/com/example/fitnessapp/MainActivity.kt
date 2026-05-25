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
import com.example.fitnessapp.utils.App
// import com.cactus.CactusContextInitializer // Временно отключено
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
// import jakarta.inject.Inject // Временно отключено
import kotlinx.coroutines.launch
import kotlin.getValue
@AndroidEntryPoint  // Это точка входа для DaggerHilt, указать если нужно получать инстанции для Хилт
class MainActivity : AppCompatActivity() {
// @Inject // Временно отключено
lateinit var tts:TextToSpeech // инициализируем в MainActivity потому что это долгая операция, будем держать в памяти
    private val model: MainViewModel by viewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController : NavController
    private lateinit var bottomNavigationView:BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // CactusContextInitializer.initialize(this) // Временно отключено
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        // Устанавливаем текущую активность для менеджера рекламы
        App.getAppOpenAdManager(application).setCurrentActivity(this)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController =  navHostFragment.navController

        bottomNavigationView = binding.bottomNavigationView
        
        // Кастомная обработка нажатий в BottomNavigationView для сброса стека при переключении табов
        bottomNavigationView.setOnItemSelectedListener { item ->
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(false)
                .setPopUpTo(navController.graph.startDestinationId, inclusive = false, saveState = false)
                .build()
            
            try {
                navController.navigate(item.itemId, null, navOptions)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
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
можем указывать нижнее подчеркивание, если не используем некоторые переменные методов, например
 */

    }
    

    override fun onResume() {
        super.onResume()
        // Обновляем текущую активность при возврате на экран
        App.getAppOpenAdManager(application).setCurrentActivity(this)
    }
    
    override fun onPause() {
        super.onPause()
        // Очищаем текущую активность при уходе с экрана
        App.getAppOpenAdManager(application).setCurrentActivity(null)
    }




}

