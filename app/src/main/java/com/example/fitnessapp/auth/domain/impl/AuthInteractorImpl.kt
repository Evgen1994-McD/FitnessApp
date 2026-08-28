package com.example.fitnessapp.auth.domain.impl

import com.example.fitnessapp.auth.YandexIdApiService
import com.example.fitnessapp.auth.domain.AuthInteractor
import com.example.fitnessapp.auth.domain.AuthRepository
import com.example.fitnessapp.auth.domain.UserSession
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthInteractorImpl @Inject constructor(
    private val authRepository: AuthRepository
) : AuthInteractor {

    override val userSession: StateFlow<UserSession> = authRepository.userSession

    override suspend fun saveAuthSession(userInfo: YandexIdApiService.YandexUserInfo) {
        authRepository.saveAuthSession(userInfo)
    }

    override suspend fun logout() {
        authRepository.logout()
    }
}
