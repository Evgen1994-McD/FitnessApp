package com.example.fitnessapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    private lateinit var timer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {// таймер для запуска урок 1
            }
            override fun onFinish() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
        }.start()


    }

    override fun onDestroy() { // это остановит таймер и закроет приложение если пользователь зашел и сразу вышел
        super.onDestroy()
        timer.cancel()
    }
}

