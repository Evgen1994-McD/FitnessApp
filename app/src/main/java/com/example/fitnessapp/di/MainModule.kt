package com.example.fitnessapp.di

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.room.Room
import androidx.transition.Visibility
import com.example.fitnessapp.R
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.utils.App
import com.example.fitnessapp.utils.MySoundPool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module //Мы создали модуль для даггера где будут методы которые будут инициализировтаь классы
@InstallIn(SingletonComponent::class) // Он установится как синглтон компонент, а значит он установится в класс апликейшен один раз и мы сможем получать её везде
object MainModule {
    @Provides
    @Singleton // Синглтон мы создаём один раз, чтобы не создавать каждый раз, не захламлять память
    fun provideMainDb(app: Application): MainDb { //MainDb это не сама база данных, а просто шаблон, и мы передедим настройки чтобы получить БД
        return Room.databaseBuilder(
            app,//Контекст
            MainDb::class.java, //Класс
            "fitness.db" //Имя
        ).createFromAsset("db/fitness.db").build() // Мы создаем БД не с 0, а возьмем её из Ассетс.
        //Поэтому сначала возьмём из ассетс, потом вызовем Билд
    //здесь требуется передать контекст. Но у нас это App, а он уже есть в даггер
//если нужен другой класс, то так просто не получится
            //Теперь с помощью ДаггерХилт мы сможем получить экземплят БД в любом месте приложения
        //Он будет уже инициализирован


    }

    @Provides
    @Singleton
    fun provideTTS(app: Application): TextToSpeech {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(app){
            if (TextToSpeech.SUCCESS==it){
                tts?.setLanguage(Locale.getDefault())
            }
        }
        return tts


    }

    @Provides
    @Singleton
    fun provideSoundPool(app: Application): MySoundPool {
        return MySoundPool(app)
/*
Теперь мы можем получать инстанцию класса MySoundPool везде где нам
заблагорассудится и получать звук с помощью @Inject
 */

    }


}