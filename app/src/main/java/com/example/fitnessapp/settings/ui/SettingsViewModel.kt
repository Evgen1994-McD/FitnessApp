package com.example.fitnessapp.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.exercises.domain.models.ThemeMode
import com.example.fitnessapp.settings.domain.SettingsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor
): ViewModel() {
//    val themeMutableLiveData = MutableLiveData<Boolean>()
//    val themeLiveData : LiveData<Boolean> get() = themeMutableLiveData

    val themeMode: StateFlow<ThemeMode> = settingsInteractor.getThemeMode().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )



    fun clearData() = viewModelScope.launch {
        settingsInteractor.clearData()
    }

    fun switchTheme(mode: ThemeMode) = viewModelScope.launch {
        settingsInteractor.setThemeMode(mode)
    }

//    fun controlCheckerPosition()=viewModelScope.launch{
//        val theme = settingsInteractor.controlTheme()
//       themeMutableLiveData.value = theme
//    }
//}

}