package com.example.fitnessapp.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexIdApiService @Inject constructor() {

    private val client = OkHttpClient()

    data class YandexUserInfo(
        val id: String,
        val login: String,
        val firstName: String? = null,
        val lastName: String? = null,
        val displayName: String? = null,
        val defaultEmail: String? = null,
        val realName: String? = null,
        val defaultAvatarId: String? = null,
        val isAvatarEmpty: Boolean = true
    )

    suspend fun getUserInfo(accessToken: String): YandexUserInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://login.yandex.ru/info?format=json")
                .addHeader("Authorization", "OAuth $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                if (!response.isSuccessful) return@withContext null

                val json = JSONObject(body)
                YandexUserInfo(
                    id = json.optString("id"),
                    login = json.optString("login"),
                    firstName = json.optStringOrNull("first_name"),
                    lastName = json.optStringOrNull("last_name"),
                    displayName = json.optStringOrNull("display_name"),
                    defaultEmail = json.optStringOrNull("default_email"),
                    realName = json.optStringOrNull("real_name"),
                    defaultAvatarId = json.optStringOrNull("default_avatar_id"),
                    isAvatarEmpty = json.optBoolean("is_avatar_empty", true)
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (has(name) && !isNull(name)) getString(name) else null

    companion object {
        fun avatarUrl(avatarId: String, size: String = "islands-200"): String =
            "https://avatars.yandex.net/get-yapic/$avatarId/$size"
    }
}
