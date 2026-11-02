package com.alexander.paxmentis

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PaxMentisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Muss vor jedem Datenbankzugriff geladen sein, egal von wo (Activity, Widget, ...).
        System.loadLibrary("sqlcipher")
    }
}
