package com.logbook.todo

import android.app.Activity
import android.app.Application
import android.os.Bundle

object AppLifecycleObserver : Application.ActivityLifecycleCallbacks {
    var isInForeground = false

    override fun onActivityResumed(activity: Activity) {
        isInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        isInForeground = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        isInForeground = false
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
