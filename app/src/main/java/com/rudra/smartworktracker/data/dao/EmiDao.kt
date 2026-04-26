package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.data.entity.Emi
import kotlinx.coroutines.flow.Flow

@Dao
interface EmiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmi(emi: Emi): Long

    @Update
    suspend fun updateEmi(emi: Emi)

    @Delete
    suspend fun deleteEmi(emi: Emi)

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND isActive = 1 AND isPaid = 0 ORDER BY nextDueDate ASC")
    fun getActiveEmis(): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getAllActiveEmis(): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND loanId = :loanId ORDER BY nextDueDate DESC")
    fun getEmisForLoan(loanId: Int): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE isDeleted = 0")
    fun getAllEmis(): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND id = :emiId")
    fun getEmiById(emiId: Int): Flow<Emi?>

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND nextDueDate < :currentTime AND isPaid = 0 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getOverdueEmis(currentTime: Long): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND nextDueDate BETWEEN :startTime AND :endTime AND isPaid = 0 AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getEmisDueInRange(startTime: Long, endTime: Long): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE isDeleted = 0 AND isPaid = 1 ORDER BY lastPaymentDate DESC")
    fun getPaidEmis(): Flow<List<Emi>>

    @Query("SELECT SUM(amount) FROM emis WHERE isDeleted = 0 AND isActive = 1 AND isPaid = 0")
    fun getTotalPendingEmiAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM emis WHERE isDeleted = 0 AND isActive = 1 AND isPaid = 0")
    fun getPendingEmiCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM emis WHERE isDeleted = 0 AND isActive = 1 AND isPaid = 0 AND nextDueDate < :currentTime")
    fun getOverdueEmiCount(currentTime: Long): Flow<Int>

    @Query("SELECT SUM(penaltyAmount) FROM emis WHERE isDeleted = 0 AND isPaid = 1")
    fun getTotalPenaltyCollected(): Flow<Double?>

    @Query("UPDATE emis SET isPaid = 1, isActive = 0, lastPaymentDate = :paymentDate, updatedAt = :timestamp WHERE id = :emiId")
    suspend fun markEmiAsPaid(emiId: Int, paymentDate: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE emis SET isSkipped = 1, updatedAt = :timestamp WHERE id = :emiId")
    suspend fun skipEmi(emiId: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE emis SET nextDueDate = :nextDueDate, updatedAt = :timestamp WHERE id = :emiId")
    suspend fun updateEmiDueDate(emiId: Int, nextDueDate: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM emis WHERE id = :emiId")
    suspend fun deleteEmiById(emiId: Int)
}
