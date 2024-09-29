package com.logbook.todo.ui

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import androidx.preference.PreferenceManager
import com.logbook.todo.R
import com.logbook.todo.databinding.SpinnerItemBinding

class CustomSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.spinner_item, items), FontSizeAware {

    private var fontSize: Float = 16f // Default font size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return try {
            createViewFromResource(position, convertView, parent)
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            e.printStackTrace()
            super.getView(position, convertView, parent) // Fallback to default view
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return try {
            createViewFromResource(position, convertView, parent)
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            e.printStackTrace()
            super.getDropDownView(position, convertView, parent) // Fallback to default dropdown view
        }
    }

    private fun createViewFromResource(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        return try {
            // Inflate using View Binding
            val binding = convertView?.let { SpinnerItemBinding.bind(it) }
                ?: SpinnerItemBinding.inflate(LayoutInflater.from(context), parent, false)

            // Access context and shared preferences using parent.context
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(parent.context)
            val selectedFontSizeIndex = sharedPreferences.getInt("FONT_SIZE_INDEX", 1)

            // Set font size based on the selected index
            fontSize = when (selectedFontSizeIndex) {
                0 -> context.resources.getDimension(R.dimen.font_size_small)
                1 -> context.resources.getDimension(R.dimen.font_size_medium)
                2 -> context.resources.getDimension(R.dimen.font_size_large)
                else -> context.resources.getDimension(R.dimen.font_size_medium)
            }

            // Set the text and the font size
            binding.spinnerItemText.text = items[position]
            binding.spinnerItemText.textSize = fontSize

            binding.root
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            e.printStackTrace()
            View(context) // Return an empty view on error
        }
    }

    override fun setFontSize(size: Float) {
        try {
            fontSize = size
            notifyDataSetChanged()
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            e.printStackTrace()
        }
    }
}
