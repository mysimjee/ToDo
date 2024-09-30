package com.logbook.todo.ui.task_list

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ListView

class CustomListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ListView(context, attrs, defStyleAttr) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Handle touch events if necessary
        if (ev.action == MotionEvent.ACTION_UP) {
            performClick() // Trigger performClick on ACTION_UP
        }
        return super.onTouchEvent(ev)
    }
}