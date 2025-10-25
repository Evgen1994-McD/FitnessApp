package com.example.fitnessapp.settings.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.domain.models.ThemeMode
import com.example.fitnessapp.settings.domain.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mainDb: MainDb): SettingsRepository {
//    companion object{
//        const val KEY_THEME_MODE = "theme_mode" // ключ для темы
//        const val SHARED_PREF_THEME_NAME = "app_theme_pref" // файл для темы
//
//    }

    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val themeKey = "theme_mode"

    private val _themeMode = MutableStateFlow(getStoredThemeMode())
    override fun getThemeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()


    private fun getStoredThemeMode(): ThemeMode {
        val stored = prefs.getString(themeKey, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(stored ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        android.util.Log.d("theme", "Setting theme mode to: $mode")
        prefs.edit().putString(themeKey, mode.name).apply()
        _themeMode.value = mode
        
        // Применяем тему к системным элементам
        when (mode) {
            ThemeMode.LIGHT -> {
                android.util.Log.d("theme", "Applying LIGHT theme")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            ThemeMode.DARK -> {
                android.util.Log.d("theme", "Applying DARK theme")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            ThemeMode.SYSTEM -> {
                android.util.Log.d("theme", "Applying SYSTEM theme")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

//
//    override fun controlTheme(): Boolean {
//        val themeSharedPrefs = context.getSharedPreferences(SHARED_PREF_THEME_NAME, Context.MODE_PRIVATE)
//
//        // Проверяем наличие ключа перед чтением значения
//        if (!themeSharedPrefs.contains(KEY_THEME_MODE)) {
//            // Ключ отсутствует, используем режим системы
//            val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//            when (currentNightMode) {
//                Configuration.UI_MODE_NIGHT_YES -> {
//                    switchTheme(true) // Включаем тёмную тему
//                    return true
//                }
//                Configuration.UI_MODE_NIGHT_NO -> {
//                    switchTheme(false) // Оставляем светлую тему
//                    return false
//                }
//            }
//        } else {
//            // Ключ присутствует, получаем и применяем сохранённую тему
//            val savedTheme = themeSharedPrefs.getBoolean(KEY_THEME_MODE, false)
//            switchTheme(savedTheme)
//            return savedTheme
//        }
//        return false
//    }

//    override fun saveCurrentThemeToShared(isChecked: Boolean) {
//        val themeSharedPrefs =
//            context.getSharedPreferences(SHARED_PREF_THEME_NAME, Context.MODE_PRIVATE)
//        themeSharedPrefs.run {
//            edit().putBoolean(KEY_THEME_MODE, isChecked).apply()
//        }
//
//    }


//    override fun switchTheme(savedTheme: Boolean) {
//
//        if (savedTheme != null) {
//            AppCompatDelegate.setDefaultNightMode(
//                if (savedTheme) {
//                    AppCompatDelegate.MODE_NIGHT_YES
//                } else {
//                    AppCompatDelegate.MODE_NIGHT_NO
//                }
//            )
//        }
//
//    }


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