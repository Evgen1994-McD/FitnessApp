package com.example.fitnessapp.auth.domain

import com.example.fitnessapp.auth.YandexIdApiService
import kotlinx.coroutines.flow.StateFlow

data class UserSession(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val avatarUrl: String? = null
)

interface AuthRepository {
    val userSession: StateFlow<UserSession>
    suspend fun saveAuthSession(userInfo: YandexIdApiService.YandexUserInfo)
    suspend fun logout()
}
