package com.logbook.todo.ui.about

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.logbook.todo.R
import com.logbook.todo.databinding.FragmentAboutBinding
import com.logbook.todo.ui.FontSizeAware

class AboutFragment : Fragment(), FontSizeAware {
    private var fontSize: Float = 16f
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val selectedFontSizeIndex = sharedPreferences.getInt("FONT_SIZE_INDEX", 1)

            // Set font size based on the selected index
            fontSize = when (selectedFontSizeIndex) {
                0 -> resources.getDimension(R.dimen.font_size_small)
                1 -> resources.getDimension(R.dimen.font_size_medium)
                2 -> resources.getDimension(R.dimen.font_size_large)
                else -> resources.getDimension(R.dimen.font_size_medium)
            }

            // Inflate the layout for this fragment using view binding
            _binding = FragmentAboutBinding.inflate(inflater, container, false)
            binding.root // Return the root view of the binding
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            Log.e("AboutFragment", "Error inflating layout: ${e.message}", e)
            View(inflater.context) // Creating an empty view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            setFontSize(fontSize)
        }catch (e: Exception) {
            // Handle any exceptions that occur while setting the font size
            e.printStackTrace()
        }

    }

    override fun setFontSize(size: Float) {
        try {
            fontSize = size // Update the font size
            applyFontSize() // Apply the new font size
        } catch (e: Exception) {
            // Handle any exceptions that occur while setting the font size
            e.printStackTrace()
        }
    }

    private fun applyFontSize() {
        try {
            // Set font size for all TextViews
            binding.appName.textSize = fontSize
            binding.version.textSize = fontSize
            binding.description.textSize = fontSize
            binding.developerName.textSize = fontSize
            binding.date.textSize = fontSize

        } catch (e: Exception) {
            e.printStackTrace() // Log any errors
            // Only call Toast when the fragment is attached to the activity
            if (isAdded) {
                Toast.makeText(requireContext(), getString(R.string.toast_font_size_failed_to_changed), Toast.LENGTH_SHORT).show() // Notify user of failure
            }
        }
    }

    override fun onDestroyView() {
      try {
          super.onDestroyView()
          // Clean up the binding reference
          _binding = null
      }catch (e: Exception) {
          e.printStackTrace()
      }
    }
}
