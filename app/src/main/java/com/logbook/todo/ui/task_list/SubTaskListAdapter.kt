package com.logbook.todo.ui.task_list

import android.content.Context

import android.util.TypedValue
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


        // Set layout height to wrap content but enforce a minimum height of 60dp
        val params = view.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT // Allow height to grow with content
        if (params.height < TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics).toInt()) {
            params.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics).toInt()
        }
        view.layoutParams = params



        // Enable text wrapping
        view.maxLines = Integer.MAX_VALUE // Allow as many lines as needed
        view.ellipsize = null // No ellipsis at the end, just wrap

        return view
    }
}