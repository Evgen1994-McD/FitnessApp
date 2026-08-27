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
    private val voiceTipsKey = "voice_tips_enabled"

    private val _themeMode = MutableStateFlow(getStoredThemeMode())
    override fun getThemeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()

    private val _voiceTipsEnabled = MutableStateFlow(getStoredVoiceTipsEnabled())
    override fun getVoiceTipsEnabled(): Flow<Boolean> = _voiceTipsEnabled.asStateFlow()


    private fun getStoredThemeMode(): ThemeMode {
        val stored = prefs.getString(themeKey, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(stored ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    private fun getStoredVoiceTipsEnabled(): Boolean {
        return prefs.getBoolean(voiceTipsKey, true) // По умолчанию включено
    }

    override suspend fun setVoiceTipsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(voiceTipsKey, enabled).apply()
        _voiceTipsEnabled.value = enabled
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

    override suspend fun openAllTrainings(onProgress: (Float) -> Unit) {
        val daysList = mainDb.daysDao.getAllDays()
        val totalDays = daysList.size
        
        daysList.forEachIndexed { index, day ->
            // Открываем только те дни что были закрыты (кроме кастомных)
            val updatedDay = if (day.zone.isNullOrEmpty()) {
                // Кастомная тренировка - всегда открыта
                day.copy(isOpen = true)
            } else {
                // Обычная тренировка - открываем только если была закрыта
                if (!day.isOpen) {
                    day.copy(isOpen = true)
                } else {
                    day // Уже открыта, оставляем как есть
                }
            }
            
            mainDb.daysDao.insertDay(updatedDay)
            
            // Обновляем прогресс
            val progress = (index + 1).toFloat() / totalDays.toFloat()
            onProgress(progress)
        }
    }
}