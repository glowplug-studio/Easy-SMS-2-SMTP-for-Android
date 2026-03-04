package com.glowplug.sms2smtp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsManager(private val context: Context) {
    
    companion object {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val SMTP_HOST = stringPreferencesKey("smtp_host")
        val SMTP_PORT = stringPreferencesKey("smtp_port")
        val SMTP_USER = stringPreferencesKey("smtp_user")
        val SMTP_PASS = stringPreferencesKey("smtp_pass")
        val FROM_EMAIL = stringPreferencesKey("from_email")
        val TO_EMAIL = stringPreferencesKey("to_email")
        val SUBJECT_TEMPLATE = stringPreferencesKey("subject_template")
    }

    val isEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_ENABLED] ?: false }
    val smtpHostFlow: Flow<String> = context.dataStore.data.map { it[SMTP_HOST] ?: "" }
    val smtpPortFlow: Flow<String> = context.dataStore.data.map { it[SMTP_PORT] ?: "465" }
    val smtpUserFlow: Flow<String> = context.dataStore.data.map { it[SMTP_USER] ?: "" }
    val smtpPassFlow: Flow<String> = context.dataStore.data.map { it[SMTP_PASS] ?: "" }
    val fromEmailFlow: Flow<String> = context.dataStore.data.map { it[FROM_EMAIL] ?: "" }
    val toEmailFlow: Flow<String> = context.dataStore.data.map { it[TO_EMAIL] ?: "" }
    val subjectTemplateFlow: Flow<String> = context.dataStore.data.map { it[SUBJECT_TEMPLATE] ?: "New SMS from {SENDER}" }

    suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_ENABLED] = enabled }
    }

    suspend fun saveSmtpSettings(
        host: String, port: String, user: String, pass: String,
        from: String, to: String, subject: String
    ) {
        context.dataStore.edit {
            it[SMTP_HOST] = host
            it[SMTP_PORT] = port
            it[SMTP_USER] = user
            it[SMTP_PASS] = pass
            it[FROM_EMAIL] = from
            it[TO_EMAIL] = to
            it[SUBJECT_TEMPLATE] = subject
        }
    }
}
