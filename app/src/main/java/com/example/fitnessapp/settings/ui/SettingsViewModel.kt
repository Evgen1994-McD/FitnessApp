package com.example.fitnessapp.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    val themeMutableLiveData = MutableLiveData<Boolean>()
    val themeLiveData : LiveData<Boolean> get() = themeMutableLiveData

     fun clearData() = viewModelScope.launch{
settingsInteractor.clearData()
    }

     fun switchTheme(theme: Boolean)=viewModelScope.launch{
        settingsInteractor.switchTheme(theme)
    }

    fun controlCheckerPosition()=viewModelScope.launch{
        val theme = settingsInteractor.controlTheme()
       themeMutableLiveData.value = theme
    }
}