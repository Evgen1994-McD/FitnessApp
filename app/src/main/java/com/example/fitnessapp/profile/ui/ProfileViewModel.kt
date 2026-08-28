package com.example.fitnessapp.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.auth.YandexAuthManager
import com.example.fitnessapp.auth.domain.AuthInteractor
import com.example.fitnessapp.auth.domain.UserSession
import com.yandex.authsdk.YandexAuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val yandexAuthManager: YandexAuthManager
) : ViewModel() {

    val userSession: StateFlow<UserSession> = authInteractor.userSession

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onAuthResult(result: YandexAuthResult) {
        viewModelScope.launch {
            try {
                val userInfo = yandexAuthManager.extractUserInfoFromResult(result)
                if (userInfo != null) {
                    authInteractor.saveAuthSession(userInfo)
                    _errorMessage.value = null
                }
                // Cancelled -> userInfo == null, тихо остаёмся в гостевом состоянии, без ошибки
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка авторизации"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authInteractor.logout()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
