package com.logbook.todo.ui.task_list

import android.content.SharedPreferences
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.logbook.todo.R
import com.logbook.todo.databinding.FragmentTaskListBinding
import com.logbook.todo.ui.FontSizeAware

class TaskListFragment : Fragment(), FontSizeAware {

    private var fontSize: Float = 16f // Default font size


    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val taskListViewModel: TaskListViewModel by viewModels()

    private lateinit var taskListAdapter: TaskListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try{
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val selectedFontSizeIndex = sharedPreferences.getInt("FONT_SIZE_INDEX", 1)

            // Set font size based on the selected index
            fontSize = when (selectedFontSizeIndex) {
                0 -> resources.getDimension(R.dimen.font_size_small)
                1 -> resources.getDimension(R.dimen.font_size_medium)
                2 -> resources.getDimension(R.dimen.font_size_large)
                else -> resources.getDimension(R.dimen.font_size_medium)
            }

            // Observe the tasksByDate LiveData from the ViewModel
            taskListViewModel.tasksByDate.observe(viewLifecycleOwner) { tasksByDate ->
                try {
                    if (tasksByDate.isNotEmpty()) {
                        // Flatten the tasks to a single list (if grouping by date isn't necessary in the adapter)
                        val allTasks = tasksByDate.values.flatten()
                        taskListAdapter.submitList(allTasks)
                    } else {
                        taskListAdapter.submitList(emptyList())
                        Toast.makeText(requireContext(), getString(R.string.no_tasks_available), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    taskListAdapter.submitList(emptyList())
                    Toast.makeText(requireContext(), "Error observing tasks", Toast.LENGTH_SHORT).show()
                }
            }

            _binding = FragmentTaskListBinding.inflate(inflater, container, false)
            return binding.root
        }  catch (e: Exception){
            Log.e("TaskFragment", "Error in onCreateView: ${e.message}", e)
            return View(inflater.context) // Creating an empty view
        }
    }

    override fun setFontSize(size: Float) {
        fontSize = size // Update the font size
        applyFontSize() // Apply the new font size
    }

    private fun applyFontSize() {
        try {
            // Update font size of TextViews in the layout
            binding.recyclerViewTasks.apply {
                taskListAdapter.setFontSize(fontSize)
            }

            binding.checkBoxShowCompleted.textSize = fontSize
        } catch (e: Exception) {
            Log.e("TaskFragment", "Error applying font size: ${e.message}", e)
        }
    }

    override fun onResume() {
        try {
            super.onResume()
            taskListViewModel.updateShowCompletedTasks(binding.checkBoxShowCompleted.isChecked)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading tasks", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       try {
           super.onViewCreated(view, savedInstanceState)

           setupRecyclerView()




           // Action Button click listener  for checkbox option
           binding.checkBoxShowCompleted.setOnCheckedChangeListener { _, isChecked ->
               try {
                   taskListViewModel.updateShowCompletedTasks(isChecked)
               } catch (e: Exception) {
                   Log.e("Task List", "Error updating checkbox value: ${e.message}")
               }
           }

           // Floating Action Button click listener (add new task)
           binding.fab.setOnClickListener {
               try {
                   val bundle = Bundle().apply {
                       putLong("taskId", 1234L)
                   }
                   findNavController().navigate(R.id.addTaskFragment, bundle)
               } catch (e: Exception) {
                   Log.e("Task List", "Error launching form to add task: ${e.message}")
                   Toast.makeText(requireContext(), "Error launching form to add task", Toast.LENGTH_SHORT).show()
               }
           }

           setFontSize(fontSize)
       } catch (e: Exception) {
           Log.e("Task List", "Error displaying task list view: ${e.message}")
       }
    }

    private fun setupRecyclerView() {
        try {
            taskListAdapter = context?.let {
                TaskListAdapter(
                    viewModel = taskListViewModel,
                    it,
                    priorities = resources.getStringArray(R.array.priority_array),
                    onEditFunction = { taskId: Long ->
                        try {
                            val bundle = Bundle().apply {
                                putLong("taskId", taskId)
                            }
                            findNavController().navigate(R.id.editTaskFragment, bundle)
                        } catch (e: Exception) {
                            Log.e("TaskListAdapter", "Error setting up  TaskListAdapter: ${e.message}")
                            Toast.makeText(requireContext(), "Error setting up TaskListAdapter", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }!!

            binding.recyclerViewTasks.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = taskListAdapter
            }
        } catch (e: Exception) {
            Log.e("TaskListAdapter", "Error creating TaskListAdapter: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
