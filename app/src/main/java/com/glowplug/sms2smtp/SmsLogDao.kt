package com.glowplug.sms2smtp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SmsLogEntity>>

    @Insert
    suspend fun insert(log: SmsLogEntity): Long

    @Update
    suspend fun update(log: SmsLogEntity)

    @Query("DELETE FROM sms_logs")
    suspend fun deleteAllLogs()
}
