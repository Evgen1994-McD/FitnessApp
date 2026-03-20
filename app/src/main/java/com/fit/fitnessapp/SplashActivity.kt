package com.fit.fitnessapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    private val model: SplashViewModel by viewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        
        // Инициализируем UI элементы
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        
        // Наблюдаем за прогрессом
        model.progress.observe(this) { progress ->
            progressBar.progress = progress
        }
        
        model.progressText.observe(this) { text ->
            progressText.text = text
        }
        
        lifecycleScope.launch {
            model.controlFirstCheck()
            
            // Ждем завершения загрузки
            kotlinx.coroutines.delay(2000) // Даем время на анимацию и завершение процессов
            
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() { 
        super.onDestroy()
    }
}
