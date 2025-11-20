package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.model.Colleague
import com.rudra.smartworktracker.model.DailyJournal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyJournalDao {

    @Query("SELECT * FROM daily_journals WHERE date = :date")
    fun getJournalForDate(date: LocalDate): Flow<DailyJournal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJournal(journal: DailyJournal)

    @Delete
    suspend fun deleteJournal(journal: DailyJournal)

    @Query("SELECT * FROM daily_journals WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getJournalsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyJournal>>

    @Query("SELECT * FROM daily_journals WHERE morningIntention LIKE :query OR eveningReflection LIKE :query OR gratitude LIKE :query ORDER BY date DESC")
    fun searchJournals(query: String): Flow<List<DailyJournal>>

    @Query("SELECT * FROM daily_journals ORDER BY date DESC")
    fun getAllJournals(): Flow<List<DailyJournal>>
}

