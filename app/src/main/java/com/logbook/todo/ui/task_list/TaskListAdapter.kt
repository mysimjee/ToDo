package com.logbook.todo.ui.task_list

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.logbook.todo.R
import com.logbook.todo.databinding.ItemTaskCardBinding
import com.logbook.todo.database.entities.TaskWithSubTasks
import com.logbook.todo.ui.FontSizeAware
import java.time.format.DateTimeFormatter

class TaskListAdapter(
    private val viewModel: TaskListViewModel,
    private val context: Context,
    private val priorities: Array<String>,
    private val onEditFunction: (Long) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>(), FontSizeAware {

    private var fontSize: Float = 16f // Default font size


    private var tasks: List<TaskWithSubTasks> = listOf()
    private val expandedPositionSet = mutableSetOf<Int>() // To track expanded items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
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


            val binding = ItemTaskCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TaskViewHolder(binding, priorities, onEditFunction)

        } catch (e: Exception) {
            Log.e("TaskAdapter", "Error creating ViewHolder: ${e.message}")
            throw e // Rethrow the exception to avoid undefined behavior
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setFontSize(size: Float) {
        // Call the existing updateFontSize method
        fontSize = size
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        try {
            val taskWithSubTasks = tasks[position]
            holder.bind(taskWithSubTasks, position)

            // Set the font size for the current ViewHolder
            holder.setFontSize(fontSize)

        } catch (e: Exception) {
            Log.e("TaskAdapter", "Error binding task at position $position: ${e.message}")
        }
    }


    override fun getItemCount(): Int {
        return try {
            tasks.size
        } catch (e: Exception) {
            // Log the exception (optional)
            e.printStackTrace()

            // Return 0 or any fallback value in case of an error
            0
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTasks: List<TaskWithSubTasks>) {
        try {
            tasks = newTasks
            notifyDataSetChanged()
        } catch (e: Exception) {
            // Handle the exception, e.g., log it or show a message
            Log.e("TaskAdapter", "Error updating task list", e)
        }
    }


    inner class TaskViewHolder(
        private val binding: ItemTaskCardBinding,
        private val priorities: Array<String>,
        private val onEditFunction: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) , FontSizeAware {

        @SuppressLint("NotifyDataSetChanged")
        override fun setFontSize(size: Float) {
            // Set the font size for each TextView and Chip in this ViewHolder
            binding.textViewTaskName.textSize = size
            binding.textViewCompletionDate.textSize = size
            binding.textViewPriority.textSize = size
            binding.checkBoxTaskCompletionStatus.textSize = size

            binding.buttonEdit.textSize = size
            binding.buttonDelete.textSize = size

            binding.textViewSubTask.textSize = size

            // Set font size for each Chip in the ChipGroup
            for (i in 0 until binding.taskChipGroupTags.childCount) {
                val chip = binding.taskChipGroupTags.getChildAt(i) as? Chip
                chip?.textSize = size
            }

        }

        @SuppressLint("SetTextI18n")

        fun bind(taskWithSubTasks: TaskWithSubTasks, position: Int) {
            try {
                val task = taskWithSubTasks.task
                binding.textViewTaskName.text = task.name

                // Clear previous chips
                binding.taskChipGroupTags.removeAllViews()

                // Populate the ChipGroup with tags
                task.tags.forEach { tag ->
                    val chip = Chip(binding.taskChipGroupTags.context).apply {
                        text = "#$tag"
                        isCloseIconVisible = false
                        isClickable = false
                    }
                    binding.taskChipGroupTags.addView(chip)
                }

                // Expand/Collapse functionality
                val isExpanded = expandedPositionSet.contains(position)
                binding.listViewSubtasks.visibility = if (isExpanded && taskWithSubTasks.subtasks.isNotEmpty()) View.VISIBLE else View.GONE
                binding.textViewSubTask.visibility = if (isExpanded && taskWithSubTasks.subtasks.isNotEmpty()) View.VISIBLE else View.GONE
                binding.checkBoxTaskCompletionStatus.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.imageViewAttachment.visibility = if (isExpanded && task.photoAttachment != null) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                binding.buttonEdit.visibility = if (isExpanded) View.VISIBLE else View.GONE
                binding.buttonDelete.visibility = if (isExpanded) View.VISIBLE else View.GONE

                // Update expand/collapse arrow
                val iconRes = if (isExpanded) R.drawable.baseline_keyboard_arrow_up_24 else R.drawable.baseline_keyboard_arrow_down_24
                binding.btnExpandCollapse.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconRes, 0)

                binding.btnExpandCollapse.setOnClickListener {
                    if (isExpanded) {
                        expandedPositionSet.remove(position)
                    } else {
                        expandedPositionSet.add(position)
                    }
                    notifyItemChanged(position)
                }

                // Handle ListView for subtasks
                val subTaskList = taskWithSubTasks.subtasks
                if (subTaskList.isNotEmpty()) {
                    val subTaskAdapter = SubTaskListAdapter(
                        binding.listViewSubtasks.context,
                        subTaskList.map { it.name },
                        fontSize // Pass the desired font size
                    )

                    binding.listViewSubtasks.adapter = subTaskAdapter
                    binding.listViewSubtasks.isClickable = false
                    binding.listViewSubtasks.choiceMode = ListView.CHOICE_MODE_MULTIPLE

                    // Set the checked state based on the completion status of each subtask
                    for ((index, subTask) in subTaskList.withIndex()) {
                        binding.listViewSubtasks.setItemChecked(index, subTask.isCompleted)
                    }

                    binding.listViewSubtasks.setOnItemClickListener { _, _, subTaskPosition, _ ->
                        val subTask = subTaskList[subTaskPosition]

                        // Toggle the completion status based on the checkbox state
                        subTask.isCompleted = !subTask.isCompleted

                        // Update the subtask's completion status in the ViewModel
                        viewModel.updateSubTaskCompletionStatus(subTask.id, subTask.isCompleted)

                        // Update the ListView item to reflect the completion status visually
                        binding.listViewSubtasks.setItemChecked(subTaskPosition, subTask.isCompleted)
                    }
                }


                // Load attachment image from URI (if available)
                task.photoAttachment?.let { uri ->
                    binding.imageViewAttachment.setImageURI(uri) // Uncomment this if you're using URI
                }

                binding.buttonDelete.setOnClickListener {
                    showDeleteConfirmationDialog(binding.root, taskWithSubTasks)
                }

                // Format and display the completion date
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                binding.textViewCompletionDate.text = binding.root.context.getString(
                    R.string.completion_date_format,
                    task.completionDate?.format(dateFormatter)
                )

                // Assuming task.priority is an integer representing the index
                binding.textViewPriority.text = binding.root.context.getString(
                    R.string.priority_label,
                    priorities[task.priority]
                )


                //Remove listener before setting state and then re-attach it
                binding.checkBoxTaskCompletionStatus.setOnCheckedChangeListener(null)
                binding.checkBoxTaskCompletionStatus.isChecked = task.isCompleted
                binding.checkBoxTaskCompletionStatus.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTaskCompletionStatus(task.id, isChecked, context)
                    viewModel.updateSubTasksCompletionStatusByTaskId(task.id, isChecked)

                    val messageResId = if (isChecked) R.string.task_completed_message else R.string.task_incomplete_message
                    val message = binding.root.context.getString(messageResId, task.name)
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }

                binding.buttonEdit.setOnClickListener {
                    onEditFunction(task.id)
                }


                val subTasksText = taskWithSubTasks.subtasks.joinToString(separator = "\n") { subTask ->
                    "- Subtask: ${subTask.name}, Completed: ${if (subTask.isCompleted) "Yes" else "No"}"
                }
                val tagsText = task.tags.joinToString(separator = ", ") { "#$it" }


                // Set a click listener on the root view
                binding.root.setOnClickListener {
                    // Trigger the btnExpandCollapse button's click action
                    binding.btnExpandCollapse.performClick()
                }

                // Long press to copy task details to clipboard
                binding.root.setOnLongClickListener {
                    val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText(
                        "Task Details",
                        "Task: ${task.name}\n" +
                                "Priority: ${priorities[task.priority]}\n" +
                                "Completion Date: ${task.completionDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}\n" +
                                "Tags: $tagsText\n" +
                                "Subtasks:\n$subTasksText \n" +
                                "Completion Status: ${if (task.isCompleted) "Completed" else "Incomplete"}\n"

                    )
                    clipboard.setPrimaryClip(clipData)

                    // Show confirmation to the user
                    Snackbar.make(binding.root, R.string.task_copied_to_clipboard_message, Snackbar.LENGTH_SHORT).show()

                    true // Indicate that the long press was handled
                }

            } catch (e: Exception) {
                Log.e("TaskAdapter", "Error binding task at position $position: ${e.message}")
            }
        }


        private fun showDeleteConfirmationDialog(view: View, taskWithSubTasks: TaskWithSubTasks) {
            AlertDialog.Builder(view.context)
                .setTitle(R.string.delete_task_title)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    try {
                        deleteTask(taskWithSubTasks)
                        Snackbar.make(view, R.string.task_deleted_message, Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Optional: Log the exception for debugging purposes
                        e.printStackTrace()

                        // Notify the user of the error
                        Snackbar.make(view, R.string.task_deletion_failed_message, Snackbar.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteTask(taskWithSubTasks: TaskWithSubTasks) {
        try {
            viewModel.deleteTask(taskWithSubTasks.task, context)
            notifyDataSetChanged()
        } catch (e: Exception) {
            // Log the exception (optional)
            e.printStackTrace()
        }
    }


}
