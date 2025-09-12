package com.example.fitnessapp.settings.domain.impl

import com.example.fitnessapp.settings.data.SettingsRepositoryImpl
import com.example.fitnessapp.settings.domain.SettingsInteractor
import com.example.fitnessapp.settings.domain.SettingsRepository
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
): SettingsInteractor{
    override suspend fun clearData() {
settingsRepository.clearData()
    }

    override fun controlTheme() {
        settingsRepository.controlTheme()
    }

    override fun switchTheme(theme: Boolean) {
   settingsRepository.switchTheme(theme)
    }


}