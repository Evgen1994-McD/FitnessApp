package com.example.fitnessapp.auth.data

import android.content.Context
import android.content.SharedPreferences
import com.example.fitnessapp.auth.YandexIdApiService
import com.example.fitnessapp.auth.domain.AuthRepository
import com.example.fitnessapp.auth.domain.UserSession
import com.example.fitnessapp.security.SecurityUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val IS_AUTHENTICATED = "is_authenticated"
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_EMAIL = "user_email"
        private const val AVATAR_URL = "avatar_url"
    }

    private fun prefs(): SharedPreferences =
        SecurityUtils.getEncryptedSharedPreferences(context, PREFS_NAME)

    private val _userSession = MutableStateFlow(loadSession())
    override val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private fun loadSession(): UserSession {
        val p = prefs()
        return UserSession(
            isAuthenticated = p.getBoolean(IS_AUTHENTICATED, false),
            userId = p.getString(USER_ID, null),
            userName = p.getString(USER_NAME, null),
            userEmail = p.getString(USER_EMAIL, null),
            avatarUrl = p.getString(AVATAR_URL, null)
        )
    }

    override suspend fun saveAuthSession(userInfo: YandexIdApiService.YandexUserInfo) {
        val avatarUrl = if (!userInfo.isAvatarEmpty && userInfo.defaultAvatarId != null) {
            YandexIdApiService.avatarUrl(userInfo.defaultAvatarId)
        } else {
            null
        }

        val name = userInfo.displayName
            ?: userInfo.realName
            ?: listOfNotNull(userInfo.firstName, userInfo.lastName)
                .joinToString(" ")
                .ifBlank { null }
            ?: userInfo.login

        val email = userInfo.defaultEmail ?: userInfo.login

        prefs().edit()
            .putBoolean(IS_AUTHENTICATED, true)
            .putString(USER_ID, userInfo.id)
            .putString(USER_NAME, name)
            .putString(USER_EMAIL, email)
            .putString(AVATAR_URL, avatarUrl)
            .apply()

        _userSession.value = UserSession(
            isAuthenticated = true,
            userId = userInfo.id,
            userName = name,
            userEmail = email,
            avatarUrl = avatarUrl
        )
    }

    override suspend fun logout() {
        prefs().edit().clear().apply()
        _userSession.value = UserSession()
    }
}
