plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")      // для KSP
    id("com.google.dagger.hilt.android") // для Dagger Hilt
}

android {
    namespace = "com.example.fitnessapp"
    compileSdk = 35




    defaultConfig {
        applicationId = "com.example.fitnessapp"
        minSdk = 29
        targetSdk = 35
        versionCode = 3
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }

}

dependencies {
    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Графики MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Календарь
    implementation("com.applandeo:material-calendar-view:1.9.2")
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Room
    val roomVersion = "2.5.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Dagger Hilt
    val daggerVersion = "2.56.1"
    implementation("com.google.dagger:hilt-android:$daggerVersion")
    ksp("com.google.dagger:hilt-compiler:$daggerVersion")

    // Core dependencies for Composable UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

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
    //Lottie Animation
    implementation(libs.dotlottie.android)
    implementation("com.github.LottieFiles:dotlottie-android:0.4.1")
    implementation("com.airbnb.android:lottie:3.4.0")


    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.23")  // GIF-библиотека (не рекомендуется использовать такую старую версию!)
}