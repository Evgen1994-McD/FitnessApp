package com.example.fitnessapp.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.settings.domain.SettingsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor
): ViewModel() {

     fun clearData() = viewModelScope.launch{
settingsInteractor.clearData()
    }

    fun switchTheme(theme: Boolean){
        settingsInteractor.switchTheme(theme)
    }
}