package com.logbook.todo

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class ToDoApplication : Application() {

    companion object {
        private const val PREFS_DARK_MODE = "DARK_MODE"
        private const val PREFS_FONT_SIZE_INDEX = "FONT_SIZE_INDEX"
    }

    private var selectedFontSizeIndex: Int = 1 // Default to Medium
    private var isDarkModeEnabled: Boolean = false // Default to false

    override fun onCreate() {
        super.onCreate()


        try {
            registerActivityLifecycleCallbacks(AppLifecycleObserver)
            TaskRepository.initialize(this)

            // Initialize SharedPreferences
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            selectedFontSizeIndex = sharedPreferences.getInt(PREFS_FONT_SIZE_INDEX, 1) // Default to Medium
            isDarkModeEnabled = sharedPreferences.getBoolean(PREFS_DARK_MODE, false) // Default to false

            // Set the theme based on the preference
            setNightMode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNightMode() {
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // Enable dark mode
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Disable dark mode
        }
    }
}
