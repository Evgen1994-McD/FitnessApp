package com.example.fitnessapp.settings.domain

interface SettingsRepository {
    fun controlTheme(): Boolean
    fun customTraining()
    suspend fun clearData()
    fun saveCurrentThemeToShared(isChecked: Boolean)
    fun switchTheme(savedTheme: Boolean)
}