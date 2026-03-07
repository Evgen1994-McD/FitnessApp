package com.example.fitnessapp.settings.domain

import com.example.fitnessapp.exercises.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsInteractor {
    suspend fun clearData()

    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    
    suspend fun openAllTrainings(onProgress: (Float) -> Unit = {})
}