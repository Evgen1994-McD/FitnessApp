package com.example.fitnessapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.utils.App
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    private val  model: SplashViewModel by viewModels()
    private lateinit var timer: CountDownTimer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        
        // Устанавливаем текущую активность для менеджера рекламы
        App.getAppOpenAdManager(application).setCurrentActivity(this)

        lifecycleScope.launch {
            model.controlFirstCheck()

            timer = object : CountDownTimer(0, 1) {
                override fun onTick(millisUntilFinished: Long) {// таймер для запуска урок 1
                }
                override fun onFinish() {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
            }.start()

        }




    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем текущую активность при возврате на экран
        App.getAppOpenAdManager(application).setCurrentActivity(this)
    }

    override fun onDestroy() { // это остановит таймер и закроет приложение если пользователь зашел и сразу вышел
        super.onDestroy()
        timer.cancel()
        // Очищаем текущую активность
        App.getAppOpenAdManager(application).setCurrentActivity(null)
    }
}

