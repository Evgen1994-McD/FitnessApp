package com.example.fitnessapp.settings.domain

interface SettingsInteractor {
    suspend fun clearData()
    fun controlTheme(): Boolean
    suspend fun switchTheme(theme: Boolean)
}