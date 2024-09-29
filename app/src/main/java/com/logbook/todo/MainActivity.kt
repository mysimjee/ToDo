package com.logbook.todo

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.preference.PreferenceManager

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.Manifest

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.logbook.todo.databinding.ActivityMainBinding
import com.logbook.todo.ui.FontSizeAware


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val fontSizes: Array<String> by lazy {
        resources.getStringArray(R.array.font_sizes)
    }
    private var selectedFontSizeIndex = 1 // Default to Medium




    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001

        private const val PREFS_DARK_MODE = "DARK_MODE"
        private const val PREFS_FONT_SIZE_INDEX = "FONT_SIZE_INDEX"
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            applyFontSize()
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)

            val navController = findNavController(R.id.nav_host_fragment_content_main)
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            navController.addOnDestinationChangedListener { _, _, _ ->
                applyFontSize()
            }

            // Load font size preference
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            selectedFontSizeIndex = sharedPreferences.getInt(PREFS_FONT_SIZE_INDEX, 1) // Default to Medium
            applyFontSize()

            // Check for permission and request if not granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_CODE_PERMISSIONS)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error initializing the app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == REQUEST_CODE_PERMISSIONS) {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission Granted To Access Gallery", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "Permission Denied To Access Gallery", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error handling permissions: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        try {
            menuInflater.inflate(R.menu.menu_main, menu)
            updateDarkModeMenuItem(menu)
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error creating options menu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return true
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            val aboutMenuItem = menu.findItem(R.id.action_about)

            // Check if the current fragment is AboutFragment
            val currentDestination = navController.currentDestination
            if (currentDestination?.id == R.id.aboutFragment) {
                aboutMenuItem.isVisible = false // Hide About menu item
            } else {
                aboutMenuItem.isVisible = true // Show About menu item otherwise
            }

            return super.onPrepareOptionsMenu(menu)
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error preparing menu: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            when (item.itemId) {
                R.id.action_about -> {
                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.aboutFragment)
                    true
                }
                R.id.action_dark_mode -> {
                    toggleDarkMode()
                    true
                }
                R.id.action_font_size -> {
                    showFontSizeDialog()
                    true
                }
                R.id.action_exit -> {
                    showExitDialog()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error handling menu item selection: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun showExitDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_title)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(R.string.button_yes) { _, _ -> finish() } // Close the activity
                .setNegativeButton(R.string.button_no) { dialog, _ -> dialog.dismiss() } // Dismiss the dialog
                .create()
                .show()
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, getString(R.string.toast_error_showing_dialog), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setDarkMode(isDarkMode: Boolean) {
        try {
            AppCompatDelegate.setDefaultNightMode(if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            })
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }

    private fun toggleDarkMode() {
        try {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkModeEnabled = currentNightMode == Configuration.UI_MODE_NIGHT_YES
            setDarkMode(!isDarkModeEnabled)
            saveThemePreference(!isDarkModeEnabled)
            recreate() // Recreate the activity to apply changes

            val message = if (isDarkModeEnabled) {
                getString(R.string.toast_light_mode)
            } else {
                getString(R.string.toast_dark_mode)
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        try {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            with(sharedPreferences.edit()) {
                putBoolean(PREFS_DARK_MODE, isDarkMode)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }

    private fun showFontSizeDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialog_title_select_font_size)
                .setSingleChoiceItems(fontSizes, selectedFontSizeIndex) { _, which ->
                    selectedFontSizeIndex = which
                }
                .setPositiveButton(R.string.button_ok) { dialog, _ ->
                    saveFontSizePreference(selectedFontSizeIndex)
                    applyFontSize()
                    dialog.dismiss()

                    // Show toast message for font size change
                    val fontSizeMessage = getString(R.string.toast_font_size_change, fontSizes[selectedFontSizeIndex])
                    Toast.makeText(this, fontSizeMessage, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.button_cancel) { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error showing font size dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFontSizePreference(index: Int) {
        try {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            with(sharedPreferences.edit()) {
                putInt(PREFS_FONT_SIZE_INDEX, index)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }

    private fun applyFontSize() {
        try {
            val fontSize = when (selectedFontSizeIndex) { // Set font size based on the selected index
                0 -> resources.getDimension(R.dimen.font_size_small) // Small
                1 -> resources.getDimension(R.dimen.font_size_medium) // Medium
                2 -> resources.getDimension(R.dimen.font_size_large) // Large
                else -> resources.getDimension(R.dimen.font_size_medium) // Default
            }

            // Get the NavHostFragment and its child fragments
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
            val fragmentManager = navHostFragment.childFragmentManager

            // Iterate over the child fragments
            for (fragment in fragmentManager.fragments) {
                if (fragment is FontSizeAware) {
                    fragment.setFontSize(fontSize)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            Toast.makeText(this, "Error applying font size: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDarkModeMenuItem(menu: Menu?) {
        try {
            menu?.findItem(R.id.action_dark_mode)?.title = if (isDarkModeEnabled()) {
                getString(R.string.menu_switch_to_light_mode)
            } else {
                getString(R.string.menu_switch_to_dark_mode)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
        }
    }

    private fun isDarkModeEnabled(): Boolean {
        return try {
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            false // Return a default value in case of error
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigateUp() || super.onSupportNavigateUp()
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            false // Return a default value in case of error
        }
    }
}