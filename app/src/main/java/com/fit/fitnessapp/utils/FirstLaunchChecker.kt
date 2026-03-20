package com.fit.fitnessapp.utils

import android.content.Context
import com.fit.fitnessapp.SplashViewModel.Companion.FIRST_LAUNCH_KEY
import com.fit.fitnessapp.SplashViewModel.Companion.PREFS_NAME

object FirstLaunchChecker {
    fun isFirstLaunch(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirst = sharedPref.getBoolean(FIRST_LAUNCH_KEY, true)
        return isFirst
    }

    fun markAsLaunched(context: Context) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(FIRST_LAUNCH_KEY, false)
        editor.apply()
    }
}