package com.example.fitnessapp.utils

import android.content.Context
import com.example.fitnessapp.utils.MainViewModel.Companion.FIRST_LAUNCH_KEY
import com.example.fitnessapp.utils.MainViewModel.Companion.PREFS_NAME

object FirstLaunchChecker {
    fun isFirstLaunch(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !sharedPref.getBoolean(FIRST_LAUNCH_KEY, false)
    }

    fun markAsLaunched(context: Context) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(FIRST_LAUNCH_KEY, true)
        editor.apply()
    }
}