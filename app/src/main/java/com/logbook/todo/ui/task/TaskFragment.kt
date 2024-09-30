package com.logbook.todo.ui.task




import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip

import com.google.android.material.snackbar.Snackbar
import com.logbook.todo.R
import com.logbook.todo.databinding.DialogAddSubtaskBinding
import com.logbook.todo.databinding.FragmentTaskBinding
import com.logbook.todo.database.entities.SubTask
import com.logbook.todo.ui.CustomSpinnerAdapter
import com.logbook.todo.ui.FontSizeAware
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class TaskFragment : Fragment(), FontSizeAware {
    private var fontSize: Float = 16f

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var subTaskAdapter: SubTaskAdapter

    private lateinit var priorityAdapter: CustomSpinnerAdapter

    // Use Safe Args to get taskId
    private var taskId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val selectedFontSizeIndex = sharedPreferences.getInt("FONT_SIZE_INDEX", 1)

            // Set font size based on the selected index
            fontSize = when (selectedFontSizeIndex) {
                0 -> resources.getDimension(R.dimen.font_size_small)
                1 -> resources.getDimension(R.dimen.font_size_medium)
                2 -> resources.getDimension(R.dimen.font_size_large)
                else -> resources.getDimension(R.dimen.font_size_medium)
            }

            // Check if taskId is provided and load task
            arguments?.let {
                taskId = it.getLong("taskId", -1) // Default value -1 if not found
            }

            // Check if taskId is valid and load task
            taskId?.let { id ->
                if (id != 1234L) {
                    viewModel.loadTaskWithSubTasks(id)
                }
            }

            // Initialize the image picker launcher
            pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    // Copy the image to the app's folder and get the new URI
                    val copiedUri = copyImageToAppFolder(it)
                    copiedUri?.let { newUri ->
                        viewModel.attachPhoto(newUri) // Update ViewModel with the URI of the copied photo
                    }
                }
            }

            _binding = FragmentTaskBinding.inflate(inflater, container, false)

            taskId?.let { id ->
                if (id != 1234L) {
                    binding.buttonSaveTask.text = getString(R.string.button_update_task)
                } else {
                    binding.buttonSaveTask.text = getString(R.string.button_save_task)
                }
            }
            return binding.root
        } catch (e: Exception) {
            // Handle the exception (log it, show a message to the user, etc.)
            Log.e("TaskFragment", "Error in onCreateView: ${e.message}", e)
            // Optionally, return a fallback view or handle the error appropriately
            fontSize = resources.getDimension(R.dimen.font_size_medium) // Fallback to default size on error
            return View(inflater.context) // Creating an empty view
        }
    }

    private fun copyImageToAppFolder(uri: Uri): Uri? {
        val appFolder = File(requireContext().filesDir, "images")
        if (!appFolder.exists()) {
            appFolder.mkdirs() // Create the folder if it doesn't exist
        }

        val fileName = "${System.currentTimeMillis()}.jpg"
        val destinationFile = File(appFolder, fileName)

        return try {
            // Open an InputStream from the content URI
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(destinationFile)

            // Copy the data from the InputStream to the OutputStream
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Return the URI of the copied file
            Uri.fromFile(destinationFile)

        } catch (e: IOException) {
            Log.e("ImageCopy", "Failed to copy image: ${e.localizedMessage}")
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupRecyclerView()
            observeViewModel()
            setupPrioritySpinner()
            setupListeners()
            setFontSize(fontSize)
        } catch (e: Exception) {
            // Handle the exception (log it, show a message to the user, etc.)
            Log.e("TaskFragment", "Error in onViewCreated: ${e.message}", e)
            // Optionally, you can show a user-friendly message or take other actions as needed
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
            // Set font size for EditText
            binding.editTextTaskName.textSize = fontSize

            // Set font size for TextViews
            binding.textViewSubtasks.textSize = fontSize
            binding.textViewCompletionDate.textSize = fontSize
            binding.textViewPriority.textSize = fontSize
            binding.textViewSelectedTags.textSize = fontSize

            // Set font size for Buttons
            binding.buttonAttachPhoto.textSize = fontSize
            binding.buttonAddSubTask.textSize = fontSize
            binding.buttonCompletionDate.textSize = fontSize
            binding.buttonSelectTags.textSize = fontSize
            binding.buttonSaveTask.textSize = fontSize

            binding.chipGroupTags.forEach { chip ->
                if (chip is Chip) {
                    chip.textSize = fontSize // Set desired font size
                }
            }

            binding.checkBoxIsCompleted.textSize = fontSize

            subTaskAdapter.setFontSize(fontSize)

            priorityAdapter.setFontSize(fontSize)

        } catch (e: Exception) {
            e.printStackTrace() // Log any errors
            Toast.makeText(requireContext(), getString(R.string.toast_font_size_failed_to_changed), Toast.LENGTH_SHORT).show() // Notify user of failure
        }
    }

    private fun setupRecyclerView() {
        try {
            binding.recyclerViewSubtasks.layoutManager = LinearLayoutManager(requireContext())
            subTaskAdapter = SubTaskAdapter(
                emptyList(),
                onSubTaskClick = { /* Handle subtask click */ },
                onLongPress = { subTask -> showDeleteConfirmationDialog(subTask) },
                viewModel
            )
            binding.recyclerViewSubtasks.adapter = subTaskAdapter
        } catch (e: Exception) {
            // Handle the exception (log it, show a message to the user, etc.)
            Log.e("TaskFragment", "Error in setupRecyclerView: ${e.message}", e)
        }
    }


    private fun observeViewModel() {
        viewModel.task.observe(viewLifecycleOwner) { task ->
            try {
                validateTaskFields()
                val currentTaskName = binding.editTextTaskName.text.toString()

                // Check if the task name is different from the current EditText text
                if (task != null) {
                    if (task.name != currentTaskName) {
                        binding.editTextTaskName.setText(task.name)
                    }
                }

                // Update the completed status
                if (task != null) {
                    binding.checkBoxIsCompleted.isChecked = task.isCompleted
                }

                // Define the desired date format
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                if (task != null) {
                    binding.buttonCompletionDate.text = task.completionDate?.format(formatter) ?: getString(R.string.not_set)
                }

                // Check for valid priority before setting it to avoid crashes
                if (task != null) {
                    if (task.priority in 1..priorityAdapter.count) {
                        binding.spinnerPriority.setSelection(task.priority - 1)
                    } else {
                        binding.spinnerPriority.setSelection(0) // Default to the first item or any fallback logic
                    }
                }

                // Update photo attachment
                if (task != null) {
                    task.photoAttachment?.let { uri ->
                        binding.imageViewPhoto.setImageURI(uri)
                        binding.buttonAttachPhoto.text = getString(R.string.clear_attachment)
                    } ?: run {
                        binding.imageViewPhoto.setImageResource(R.drawable.baseline_no_photography_24)
                        binding.buttonAttachPhoto.text = getString(R.string.attach_photo)
                    }
                }

                // Update tags UI if necessary
                if (task != null) {
                    displaySelectedTags(task.tags)
                }
            } catch (e: Exception) {
                // Handle the exception (log it, show a message to the user, etc.)
                Log.e("TaskFragment", "Error in observeViewModel: ${e.message}", e)

            }
        }

        // Observe the subtasks LiveData
        viewModel.subtasks.observe(viewLifecycleOwner) { subtasks ->
            try {
                // Update the adapter with the new list of subtasks
                if (subtasks != null) {
                    subTaskAdapter.updateSubTasks(subtasks)
                }
            } catch (e: Exception) {
                // Handle any exceptions that may occur while updating subtasks
                Log.e("TaskFragment", "Error in observing subtasks: ${e.message}", e)
                // Optionally, you can show a user-friendly message or take other actions as needed
            }
        }
    }


    private fun setupListeners() {
        try {
            binding.editTextTaskName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Handle any logic before the text is changed, if needed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // This method is called when the text is changed
                    val taskName = s.toString()
                    viewModel.updateTaskName(taskName)
                }

                override fun afterTextChanged(s: Editable?) {
                    // Handle any logic after the text has changed, if needed
                }
            })

            binding.buttonAttachPhoto.setOnClickListener {
                try {
                    if (viewModel.task.value?.photoAttachment == null) {
                        // If no photo is attached, open the gallery using the launcher
                        pickImageLauncher.launch("image/*")
                    } else {
                        // If a photo is already attached, clear the attachment
                        clearPhotoAttachment()
                    }
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Snackbar.make(binding.root, getString(R.string.error_attaching_photo), Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.buttonCompletionDate.setOnClickListener {
                try {
                    showDateTimePickerDialog()
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Snackbar.make(binding.root, getString(R.string.error_showing_date_picker), Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.buttonSaveTask.setOnClickListener {
                try {
                    taskId?.let { id ->
                        if (id != 1234L) {
                            context?.let { viewModel.updateTask(it) }
                            Snackbar.make(binding.root, getString(R.string.task_updated_successfully), Snackbar.LENGTH_SHORT).show()
                        } else {
                            context?.let { viewModel.saveTask(it) }
                            Snackbar.make(binding.root, getString(R.string.task_saved_successfully), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Snackbar.make(binding.root, getString(R.string.failed_to_save_update_task, e.message), Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.checkBoxIsCompleted.setOnCheckedChangeListener { _, isChecked ->
                try {
                    viewModel.updateTaskCompletion(isChecked)
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Snackbar.make(binding.root, getString(R.string.error_updating_completion_status), Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.buttonAddSubTask.setOnClickListener {
                try {
                    showAddSubTaskDialog()
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Snackbar.make(binding.root, getString(R.string.error_adding_subtask), Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.buttonSelectTags.setOnClickListener {
                try {
                    showTagSelectionDialog()
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Snackbar.make(binding.root, getString(R.string.error_selecting_tags), Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.spinnerPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    try {
                        viewModel.setPriority(position + 1) // Assuming 1 is Low, 2 is Medium, etc.
                    } catch (e: Exception) {
                        // Handle the error (e.g., log it or show a Snack bar)
                        Snackbar.make(binding.root, getString(R.string.error_setting_priority), Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    try {
                        viewModel.setPriority(1)
                    } catch (e: Exception) {
                        // Handle the error (e.g., log it or show a Snack bar)
                        Snackbar.make(binding.root, getString(R.string.error_setting_priority), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            // Handle the error that might occur during setup
            Log.e("TaskFragment", "Error in setupListeners: ${e.message}", e)
            Snackbar.make(binding.root, getString(R.string.error_setting_up_listeners), Snackbar.LENGTH_SHORT).show()
        }
    }



    private fun validateTaskFields() {
        try {
            val taskName = binding.editTextTaskName.text.toString().trim()
            val tags = viewModel.task.value?.tags ?: emptyList()
            val completionDate = viewModel.task.value?.completionDate

            // Check if task name, tags, and completion date are not empty
            val isValid = taskName.isNotEmpty() && tags.isNotEmpty() && completionDate != null

            // Enable or disable the Save button based on validation result
            binding.buttonSaveTask.isEnabled = isValid

            // Only change button color if it's invalid, otherwise keep default color
            if (isValid) {
                // Restore the default button color using ContextCompat.getColor()
                val defaultBackgroundColor = ContextCompat.getColor(requireContext(), R.color.brown_notebook_primary)
                binding.buttonSaveTask.setBackgroundTintList(ColorStateList.valueOf(defaultBackgroundColor))

            } else {
                // Set a custom color for the disabled state
                val disabledColor = ContextCompat.getColor(requireContext(), R.color.button_disabled_color)
                binding.buttonSaveTask.backgroundTintList = ColorStateList.valueOf(disabledColor)
            }

        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error in validateTaskFields: ${e.message}", e)

            // disable the Save button to prevent saving invalid data
            binding.buttonSaveTask.isEnabled = false
            Snackbar.make(binding.root, getString(R.string.error_validating_task_fields), Snackbar.LENGTH_SHORT).show()
        }
    }






    private fun clearPhotoAttachment() {
        try {
            // Delete the copied photo file if it exists
            viewModel.task.value?.photoAttachment?.let { uri ->
                deletePhotoFile(uri)
            }

            // Clear the photo in the ViewModel
            viewModel.clearPhotoAttachment()

            // Reset the UI
            binding.imageViewPhoto.setImageResource(R.drawable.baseline_no_photography_24) // Reset image to default
            binding.buttonAttachPhoto.text = getString(R.string.attach_photo) // Reset button text
        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error in clearPhotoAttachment: ${e.message}", e)
            Snackbar.make(binding.root, getString(R.string.error_clearing_photo_attachment), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deletePhotoFile(uri: Uri) {
        try {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                if (file.delete()) {
                    Log.d("TaskFragment", "Photo file deleted successfully: ${file.absolutePath}")
                } else {
                    Log.e("TaskFragment", "Failed to delete photo file: ${file.absolutePath}")
                }
            } else {
                Log.d("TaskFragment", "Photo file does not exist: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("TaskFragment", "Error deleting photo file: ${e.message}", e)
        }
    }

    private fun showAddSubTaskDialog() {
        val dialogBinding = DialogAddSubtaskBinding.inflate(layoutInflater)

        // Create and set up the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_subtask))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.ok), null) // Use null here to handle click manually
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    val subTaskName = dialogBinding.editTextSubTaskName.text.toString().trim() // Trim input
                    val isCompleted = dialogBinding.checkBoxIsCompleted.isChecked

                    dialogBinding.editTextSubTaskName.textSize = fontSize
                    dialogBinding.checkBoxIsCompleted.textSize = fontSize

                    // Check if the subTaskName is not empty
                    if (subTaskName.isNotEmpty()) {
                        viewModel.task.value?.let { currentTask ->
                            // Create the subtask
                            val subTask = SubTask(
                                id = Random.nextLong(), // Generate a random ID for the subtask
                                taskId = currentTask.id,
                                name = subTaskName,
                                isCompleted = isCompleted
                            )

                            // Add the subtask to the main task and the list of subtasks
                            viewModel.addSubTask(subTask) // Adding to the main task

                            dialog.dismiss() // Dismiss the dialog after adding
                        } ?: run {
                            // Log or handle the case where the current task is null
                            Log.e("TaskFragment", "Cannot add subtask. Current task is null.")
                        }
                    } else {
                        // Show an error message (e.g., Toast) if the name is empty
                        Toast.makeText(requireContext(), getString(R.string.subtask_name_empty), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Handle the error (e.g., log it or show a Snack bar)
                    Log.e("TaskFragment", "Error adding subtask: ${e.message}", e)
                    Snackbar.make(binding.root, getString(R.string.error_adding_subtask), Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show() // Show the dialog
    }




    private fun showDateTimePickerDialog() {
        try {
            val calendar = Calendar.getInstance()
            val currentDateTime = LocalDateTime.now() // Get the current date and time

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Step 1: Show DatePickerDialog
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Step 2: Show TimePickerDialog after selecting the date
                val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    // Combine date and time into LocalDateTime
                    val selectedDateTime = LocalDateTime.of(
                        selectedYear,
                        selectedMonth + 1, // Month is 0-based, add 1
                        selectedDay,
                        selectedHour,
                        selectedMinute
                    )

                    // Step 3: Ensure selected date-time is not less than the current date-time
                    if (selectedDateTime.isBefore(currentDateTime)) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_past_date_time_selection),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        // Set the completion date using your view model
                        viewModel.setCompletionDate(selectedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

                // Show the time picker dialog
                timePickerDialog.show()
            }, year, month, day)

            // Step 4: Prevent selecting dates before today
            datePickerDialog.datePicker.minDate = calendar.timeInMillis

            // Show the date picker dialog
            datePickerDialog.show()
        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error showing date-time picker dialog: ${e.message}", e)
            Snackbar.make(
                binding.root,
                getString(R.string.error_showing_date_time_picker_dialog),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }





    private fun setupPrioritySpinner() {
        try {
            val priorities = resources.getStringArray(R.array.priority_array)
            priorityAdapter = CustomSpinnerAdapter(requireContext(), priorities.toList())
            priorityAdapter.setDropDownViewResource(R.layout.spinner_item)
            binding.spinnerPriority.adapter = priorityAdapter
        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error setting up priority spinner: ${e.message}", e)
            Snackbar.make(binding.root, getString(R.string.error_setting_up_priority_spinner), Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun showTagSelectionDialog() {
        try {
            val tags = resources.getStringArray(R.array.tag_array)
            val currentTags = viewModel.task.value?.tags ?: emptyList() // Get current tags from ViewModel

            // Initialize checkedItems based on existing tags in the ViewModel
            val checkedItems = BooleanArray(tags.size) { index ->
                currentTags.contains(tags[index])
            }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_select_tags_title)
                .setMultiChoiceItems(tags, checkedItems) { _, which, isChecked ->
                    // Update the checkedItems array when a tag is checked or unchecked
                    checkedItems[which] = isChecked
                }
                .setPositiveButton(R.string.dialog_select_tags_ok) { dialog, _ ->
                    // Collect the selected tags
                    val selectedTags = tags.filterIndexed { index, _ -> checkedItems[index] }

                    // Find tags to remove (those that are currently in the currentTags but not in selectedTags)
                    val tagsToRemove = currentTags.filter { !selectedTags.contains(it) }
                    tagsToRemove.forEach { tag ->
                        viewModel.removeTag(tag) // Remove unselected tags
                    }

                    viewModel.addTags(selectedTags) // Add the selected tags
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error showing tag selection dialog: ${e.message}", e)
            Snackbar.make(binding.root, getString(R.string.error_showing_tag_selection_dialog), Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun displaySelectedTags(tags: List<String>) {
        try {
            binding.chipGroupTags.removeAllViews() // Clear previous chips
            for (tag in tags) {
                val chip = Chip(requireContext()).apply {
                    text = tag
                    textSize = fontSize
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        viewModel.removeTag(tag)
                    }
                }
                binding.chipGroupTags.addView(chip)
            }
        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error displaying selected tags: ${e.message}", e)
            Snackbar.make(binding.root, getString(R.string.error_displaying_tags), Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun showDeleteConfirmationDialog(subTask: SubTask) {
        try {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_subtask))
                .setMessage(getString(R.string.delete_subtask_confirmation))
                .setPositiveButton(R.string.dialog_confirm_delete_subtask_yes) { _, _ ->
                    viewModel.removeSubTask(subTask)
                }
                .setNegativeButton(R.string.dialog_confirm_delete_subtask_no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            // Handle the error (e.g., log it or show a Snack bar)
            Log.e("TaskFragment", "Error showing delete confirmation dialog: ${e.message}", e)
            Snackbar.make(binding.root, getString(R.string.error_showing_dialog), Snackbar.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        try {
            super.onDestroyView()
            _binding = null
        } catch (e: Exception) {
            // Handle the error (e.g., log it)
            Log.e("TaskFragment", "Error in onDestroyView: ${e.message}", e)

        }
    }
}
