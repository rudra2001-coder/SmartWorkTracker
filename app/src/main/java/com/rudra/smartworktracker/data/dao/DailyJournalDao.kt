package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.model.DailyJournal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyJournalDao {

    @Query("SELECT * FROM daily_journals WHERE date = :date")
    fun getJournalForDate(date: LocalDate): Flow<DailyJournal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJournal(journal: DailyJournal)
}
