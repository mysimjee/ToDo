package com.logbook.todo.ui.task

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.logbook.todo.R
import com.logbook.todo.databinding.ItemSubtaskBinding
import com.logbook.todo.database.entities.SubTask
import com.logbook.todo.ui.FontSizeAware

class SubTaskAdapter(
    var subTasks: List<SubTask>,

    private val onLongPress: (SubTask) -> Unit,
    private val viewModel: TaskViewModel
) : RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder>() , FontSizeAware {

    private var fontSize: Float = 16f // Default font size


    inner class SubTaskViewHolder(private val binding: ItemSubtaskBinding) : RecyclerView.ViewHolder(binding.root) , FontSizeAware {


        init {
            try {


                binding.root.setOnLongClickListener {
                    try {
                        onLongPress(subTasks[adapterPosition])
                    } catch (e: Exception) {
                        Log.e("SubTaskAdapter", "Error on sub-task long press: ${e.message}", e)
                    }
                    true // Indicate that the long press was handled
                }
            } catch (e: Exception) {
                Log.e("SubTaskAdapter", "Error setting up click listeners: ${e.message}", e)
            }
        }

        override fun setFontSize(size: Float) {
            // Set the font size for each TextView and Chip in this ViewHolder
            binding.textViewSubtaskName.textSize = size
            binding.checkBoxSubTaskCompleted.textSize = size

        }

        fun bind(subTask: SubTask) {
            try {
                binding.textViewSubtaskName.text = subTask.name
                binding.checkBoxSubTaskCompleted.isChecked = subTask.isCompleted

                binding.checkBoxSubTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                    try {
                        viewModel.updateSubTaskCompletion(subTask, isChecked) // Call the ViewModel to update the completion status
                    } catch (e: Exception) {
                        Log.e("SubTaskAdapter", "Error updating sub-task completion: ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("SubTaskAdapter", "Error binding sub-task: ${e.message}", e)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        return try {

            // Access context and shared preferences using parent.context
            val context = parent.context
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val selectedFontSizeIndex = sharedPreferences.getInt("FONT_SIZE_INDEX", 1)

            // Set font size based on the selected index
            fontSize = when (selectedFontSizeIndex) {
                0 -> context.resources.getDimension(R.dimen.font_size_small)
                1 -> context.resources.getDimension(R.dimen.font_size_medium)
                2 -> context.resources.getDimension(R.dimen.font_size_large)
                else -> context.resources.getDimension(R.dimen.font_size_medium)
            }


            val binding = ItemSubtaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SubTaskViewHolder(binding)
        } catch (e: Exception) {
            Log.e("SubTaskAdapter", "Error inflating view holder: ${e.message}", e)
            throw e // Rethrow the exception to avoid undefined behavior
        }
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        try {
            holder.bind(subTasks[position])
        } catch (e: Exception) {
            Log.e("SubTaskAdapter", "Error binding view holder at position $position: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int = try {
        subTasks.size
    } catch (e: Exception) {
        Log.e("SubTaskAdapter", "Error getting item count: ${e.message}", e)
        0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSubTasks(newSubTasks: List<SubTask>) {
        try {
            subTasks = newSubTasks
            notifyDataSetChanged() // Refresh the adapter with the new list
        } catch (e: Exception) {
            Log.e("SubTaskAdapter", "Error updating sub-tasks: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setFontSize(size: Float) {
        fontSize = size
        notifyDataSetChanged()
    }
}
