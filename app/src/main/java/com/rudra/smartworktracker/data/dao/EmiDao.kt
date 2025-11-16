package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.data.entity.Emi
import kotlinx.coroutines.flow.Flow

@Dao
interface EmiDao {
    @Insert
    suspend fun insertEmi(emi: Emi)

    @Update
    suspend fun updateEmi(emi: Emi)

    @Query("SELECT * FROM emis WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveEmis(): Flow<List<Emi>>

    @Query("SELECT * FROM emis WHERE loanId = :loanId")
    fun getEmisForLoan(loanId: Int): Flow<List<Emi>>
}
