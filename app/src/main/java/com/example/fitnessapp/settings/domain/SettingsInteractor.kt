package com.example.fitnessapp.settings.domain

interface SettingsInteractor {
    suspend fun clearData()
    fun controlTheme()
    fun switchTheme(theme: Boolean)
}