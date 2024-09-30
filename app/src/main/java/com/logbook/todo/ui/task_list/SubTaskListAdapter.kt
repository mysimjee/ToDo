package com.logbook.todo.ui.task_list

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class SubTaskListAdapter(
    context: Context,
    subTasks: List<String>,
    private val fontSize: Float
) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, subTasks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.textSize = fontSize // Set the desired font size
        return view
    }
}