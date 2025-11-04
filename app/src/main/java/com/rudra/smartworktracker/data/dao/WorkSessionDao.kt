package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rudra.smartworktracker.model.WorkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {
    @Insert
    suspend fun insertWorkSession(workSession: WorkSession)

    @Query("SELECT * FROM work_sessions")
    fun getAllWorkSessions(): Flow<List<WorkSession>>
}
