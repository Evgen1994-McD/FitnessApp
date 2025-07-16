package com.example.fitnessapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.fitnessapp.R

class MySoundPool(private val context : Context) {
    private var soundPool: SoundPool? = null
    private var finishSoundId = 0

    init {
        val  audioAttributes = AudioAttributes.Builder()
            .setUsage(
                AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
            )
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
soundPool = SoundPool.Builder()
    .setAudioAttributes(audioAttributes)
    .setMaxStreams(1) //сколько потоков. мы будем воспроизводить 1 мелодию, ничего не будет параллельно
    .build()
        loadSound()
    }
/*
Инициализируем soundPool
выбираем атрибуты и делаем билд
 */

    private fun loadSound(){
        finishSoundId = soundPool?.load(context, R.raw.start_exercise, 1) ?: 0

        /*
        загружаем через контекст. П
        priority - это важность данной мелодии.
        Например если раздастся другая мелодия, каку. из них играть.
        Если нам нужны разные мелодии, то можно вызввать разные, тут у меня 1.
         */
    }

    fun playSound(){
        soundPool?.play(finishSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        /*
        leftVolume - громкость левый канал У нас на полную катушку
        rightVolume - громкость правый канал
        priority - приоритет
        loop - это если надо закольцевать
        rate - я так понимаю, это скорость перемотки ( скорость воспроизведения)
         */
    }
}