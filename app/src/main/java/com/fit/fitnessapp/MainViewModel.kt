package com.fit.fitnessapp

import androidx.lifecycle.ViewModel
import com.fit.fitnessapp.settings.domain.SettingsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {  // Через класс ВьюМодел мы "Сохраняем" состояние. То есть, если повернется экран - список не пропадёт и т.д. Его надо подключить к активити и к фрагментам
//    fun controlTheme(){
//        settingsInteractor.getThemeMode()
//    }



}