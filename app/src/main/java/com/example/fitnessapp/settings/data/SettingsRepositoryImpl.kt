package com.example.fitnessapp.settings.data

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.settings.domain.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mainDb: MainDb): SettingsRepository {
    companion object{
        const val KEY_THEME_MODE = "theme_mode" // ключ для темы
        const val SHARED_PREF_THEME_NAME = "app_theme_pref" // файл для темы

    }
    override fun controlTheme(): Boolean {
        val themeSharedPrefs = context.getSharedPreferences(SHARED_PREF_THEME_NAME, Context.MODE_PRIVATE)

        // Проверяем наличие ключа перед чтением значения
        if (!themeSharedPrefs.contains(KEY_THEME_MODE)) {
            // Ключ отсутствует, используем режим системы
            val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    switchTheme(true) // Включаем тёмную тему
                    return true
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    switchTheme(false) // Оставляем светлую тему
                    return false
                }
            }
        } else {
            // Ключ присутствует, получаем и применяем сохранённую тему
            val savedTheme = themeSharedPrefs.getBoolean(KEY_THEME_MODE, false)
            switchTheme(savedTheme)
            return savedTheme
        }
        return false
    }

    override fun saveCurrentThemeToShared(isChecked: Boolean) {
        val themeSharedPrefs =
            context.getSharedPreferences(SHARED_PREF_THEME_NAME, Context.MODE_PRIVATE)
        themeSharedPrefs.run {
            edit().putBoolean(KEY_THEME_MODE, isChecked).apply()
        }

    }


    override fun switchTheme(savedTheme: Boolean) {

        if (savedTheme != null) {
            AppCompatDelegate.setDefaultNightMode(
                if (savedTheme) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }

    }


    override fun customTraining() {
        TODO("Not yet implemented")
    }

    override suspend fun clearData() {
        val daysList = mainDb.daysDao.getAllDays()
        daysList.forEach { day ->
            mainDb.daysDao.insertDay(day.copy(
                doneExerciseCounter = 0,
                isDone = false
            ))

        }
        mainDb.statisticDao.clearStatistic()
    }
}