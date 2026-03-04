package com.glowplug.sms2smtp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_logs")
data class SmsLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sender: String,
    val messageBody: String,
    val timestamp: Long,
    val isForwarded: Boolean,
    val forwardedToEmail: String,
    val subject: String,
    val errorMessage: String? = null
)
