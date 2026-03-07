import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")      // для KSP
    id("com.google.dagger.hilt.android") // для Dagger Hilt
    // id("androidx.room")                // Room плагин не нужен, используем только KSP
}

android {
    namespace = "com.example.fitnessapp"
    compileSdk = 36
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.17"
    }

    defaultConfig {
        applicationId = "com.example.fitnessapp"
        minSdk = 29
        targetSdk = 33
        versionCode = 11
        versionName = "1.82"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Поддерживаемые архитектуры для нативных библиотек
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug{
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

}

dependencies {
    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Графики MPAndroidChart
    implementation(libs.mpandroidchart)

    // Календарь
    implementation(libs.material.calendar.view)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.foundation)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Core dependencies for Composable UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.compose.runtime:runtime:1.5.9")

    // Compose UI
    implementation("androidx.compose.ui:ui:1.5.9")
    // Material 3
    implementation("androidx.compose.material3:material3:1.2.1")
    // Интеграция Compose с View-системами
    implementation("androidx.compose.ui:ui-viewbinding:1.5.9")
    // Для observeAsState и ViewModel в Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    // Для collectAsStateWithLifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")
    // Для observeAsState с LiveData
    implementation("androidx.compose.runtime:runtime-livedata:1.5.9")

    //Lottie Animation
    implementation("com.airbnb.android:lottie:6.1.0")

    // Coil for image loading (including GIF from assets)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Other libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common.ktx)

    // Тестовые зависимости
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Дополнительные зависимости
    implementation("androidx.fragment:fragment-ktx:1.8.6") // Зависимость для фрагментов

    //live data
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    // Lifecycle Process для отслеживания состояния приложения
    implementation("androidx.lifecycle:lifecycle-process:2.9.1")

    // Иконки
    // Базовые иконки Material 3
    implementation("androidx.compose.material3:material3:1.2.1")
    // Дополнительные иконки (если нужны Outlined, Rounded и т.д.)
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Cactus AI
    implementation("com.cactuscompute:cactus:1.4.1-beta")
//    implementation("com.cactuscompute:cactus:1.8.0") // или последнюю


    // OkHttp для ручного скачивания моделей
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation(libs.android.gif.drawable)  // GIF-библиотека (не рекомендуется использовать такую старую версию!)

    // Yandex Mobile Ads SDK
    implementation("com.yandex.android:mobileads:7.18.0")

    // Hilt Navigation Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Если ещё нет, добавьте:
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
}