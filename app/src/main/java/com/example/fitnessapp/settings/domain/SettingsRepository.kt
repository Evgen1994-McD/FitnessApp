package com.example.fitnessapp.settings.domain

import com.example.fitnessapp.exercises.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
//    fun controlTheme(): Boolean
    fun customTraining()
    suspend fun clearData()
//    fun saveCurrentThemeToShared(isChecked: Boolean)
//    fun switchTheme(savedTheme: Boolean)
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun openAllTrainings(onProgress: (Float) -> Unit = {})
}