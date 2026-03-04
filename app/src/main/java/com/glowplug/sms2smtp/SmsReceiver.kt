package com.glowplug.sms2smtp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val app = context.applicationContext as SmsApp
            val settings = app.settingsManager
            val db = app.database.smsLogDao()

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = settings.isEnabledFlow.first()
                    if (isEnabled) {
                        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                        if (messages.isNotEmpty()) {
                            val sender = messages.first().displayOriginatingAddress ?: "Unknown"
                            val body = messages.joinToString(separator = "") { it.displayMessageBody ?: "" }

                            val host = settings.smtpHostFlow.first()
                            val port = settings.smtpPortFlow.first()
                            val user = settings.smtpUserFlow.first()
                            val pass = settings.smtpPassFlow.first()
                            val from = settings.fromEmailFlow.first()
                            val to = settings.toEmailFlow.first()
                            val subjectTemplate = settings.subjectTemplateFlow.first()

                            val subject = subjectTemplate.replace("{SENDER}", sender)

                            val logEntity = SmsLogEntity(
                                sender = sender,
                                messageBody = body,
                                timestamp = System.currentTimeMillis(),
                                isForwarded = false,
                                forwardedToEmail = to,
                                subject = subject
                            )
                            val logId = db.insert(logEntity)

                            val (success, errorMessage) = EmailSender.sendEmailSync(host, port, user, pass, from, to, subject, body)

                            db.update(logEntity.copy(id = logId.toInt(), isForwarded = success, errorMessage = errorMessage))
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
