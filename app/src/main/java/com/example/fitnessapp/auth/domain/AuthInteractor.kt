package com.example.fitnessapp.auth.domain

import com.example.fitnessapp.auth.YandexIdApiService
import kotlinx.coroutines.flow.StateFlow

interface AuthInteractor {
    val userSession: StateFlow<UserSession>
    suspend fun saveAuthSession(userInfo: YandexIdApiService.YandexUserInfo)
    suspend fun logout()
}
