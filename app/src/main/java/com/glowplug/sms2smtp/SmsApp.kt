package com.glowplug.sms2smtp

import android.app.Application

class SmsApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val settingsManager by lazy { SettingsManager(this) }
}
