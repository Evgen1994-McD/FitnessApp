package com.example.fitnessapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.fitnessapp.databinding.ActivityMainBinding
import com.example.fitnessapp.utils.App
// import com.cactus.CactusContextInitializer // Временно отключено
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // CactusContextInitializer.initialize(this) // Временно отключено
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загружаем и отображаем streak
        model.streakCount.observe(this) { streak ->
            binding.dayCounter.text = "$streak д."
        }
        model.loadStreak()

        // Наблюдаем за изменением текста TopBar для списка упражнений
        model.trainingDaysTitle.observe(this) { title ->
            if (title.isNotEmpty()) {
                binding.trainingDaysTitle.text = title
            }
        }

        // Наблюдаем за изменением текста TopBar для выполнения упражнения
        model.exerciseTitle.observe(this) { title ->
            if (title.isNotEmpty()) {
                binding.exerciseTitle.text = title
            }
        }

        // Устанавливаем текущую активность для менеджера рекламы
        App.getAppOpenAdManager(application).setCurrentActivity(this)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController =  navHostFragment.navController

        bottomNavigationView = binding.bottomNavigationView
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        // Обработка клика на аватар для открытия NavigationDrawer
        binding.avatarCard.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Обработка элементов меню NavigationDrawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // TODO: Открыть профиль
                }
                R.id.nav_settings -> {
                    navController.navigate(R.id.settingsFragment)
                }
                R.id.nav_share -> {
                    // TODO: Поделиться
                }
                R.id.nav_rate -> {
                    // TODO: Оценить приложение
                }
                R.id.nav_documents -> {
                    // TODO: Открыть документы
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

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

        // Back button for settings toolbar
        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }

        // Back button for training days toolbar
        binding.trainingDaysBackButton.setOnClickListener {
            navController.navigateUp()
        }

        // Back button for exercise toolbar
        binding.exerciseBackButton.setOnClickListener {
            navController.navigateUp()
        }

        // Back button for training list toolbar
        binding.trainingListBackButton.setOnClickListener {
            navController.navigateUp()
        }

        fun updateFragmentConstraint(topViewId: Int) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainContent)
            constraintSet.connect(
                R.id.fragmentContainerView,
                ConstraintSet.TOP,
                topViewId,
                ConstraintSet.BOTTOM,
                0
            )
            constraintSet.applyTo(binding.mainContent)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.selectedExerciseListFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.VISIBLE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.GONE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.topBar)
                }
                R.id.chooseExercisesFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.VISIBLE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.GONE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.topBar)
                }
                R.id.exListFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.GONE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.VISIBLE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.trainingDaysToolbar)
                }
                R.id.exerciseFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.GONE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.GONE
                    binding.exerciseToolbar.visibility = View.VISIBLE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.exerciseToolbar)
                }
                R.id.daysFinishFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.GONE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.VISIBLE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.trainingDaysToolbar)
                }
                R.id.settingsFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.GONE
                    binding.settingsToolbar.visibility = View.VISIBLE
                    binding.trainingDaysToolbar.visibility = View.GONE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.settingsToolbar)
                }
                R.id.trainingListFragment ->{
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.topBar.visibility = View.GONE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.GONE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.VISIBLE
                    updateFragmentConstraint(R.id.trainingListToolbar)
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    binding.topBar.visibility = View.VISIBLE
                    binding.settingsToolbar.visibility = View.GONE
                    binding.trainingDaysToolbar.visibility = View.GONE
                    binding.exerciseToolbar.visibility = View.GONE
                    binding.trainingListToolbar.visibility = View.GONE
                    updateFragmentConstraint(R.id.topBar)
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
        // Обновляем streak при возврате на экран
        model.loadStreak()
    }
    
    override fun onPause() {
        super.onPause()
        // Очищаем текущую активность при уходе с экрана
        App.getAppOpenAdManager(application).setCurrentActivity(null)
    }




}

