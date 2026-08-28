package com.example.fitnessapp.auth

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexAuthManager @Inject constructor(
    @ApplicationContext context: Context,
    private val yandexIdApiService: YandexIdApiService
) {

    private val sdk = YandexAuthSdk.create(YandexAuthOptions(context))

    fun getContract(): ActivityResultContract<YandexAuthLoginOptions, YandexAuthResult> = sdk.contract

    fun createLoginOptions(): YandexAuthLoginOptions = YandexAuthLoginOptions()

    suspend fun extractUserInfoFromResult(result: YandexAuthResult): YandexIdApiService.YandexUserInfo? {
        return when (result) {
            is YandexAuthResult.Success -> {
                val accessToken = result.token.value
                yandexIdApiService.getUserInfo(accessToken)
            }
            is YandexAuthResult.Failure -> throw result.exception
            YandexAuthResult.Cancelled -> null
        }
    }
}
