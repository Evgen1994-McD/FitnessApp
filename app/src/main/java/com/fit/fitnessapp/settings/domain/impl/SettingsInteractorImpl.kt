package com.fit.fitnessapp.settings.domain.impl

import com.fit.fitnessapp.exercises.domain.models.ThemeMode
import com.fit.fitnessapp.settings.domain.SettingsInteractor
import com.fit.fitnessapp.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
): SettingsInteractor{
    override suspend fun clearData() {
        settingsRepository.clearData()
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return settingsRepository.getThemeMode()
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }

    override suspend fun openAllTrainings(onProgress: (Float) -> Unit) {
        settingsRepository.openAllTrainings(onProgress)
    }

    override fun getVoiceTipsEnabled(): Flow<Boolean> {
        return settingsRepository.getVoiceTipsEnabled()
    }

    override suspend fun setVoiceTipsEnabled(enabled: Boolean) {
        settingsRepository.setVoiceTipsEnabled(enabled)
    }

}