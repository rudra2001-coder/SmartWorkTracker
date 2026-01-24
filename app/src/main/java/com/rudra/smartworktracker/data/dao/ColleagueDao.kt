package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.model.Colleague
import kotlinx.coroutines.flow.Flow


@Dao
interface ColleagueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColleague(colleague: Colleague)

    @Update
    suspend fun update(colleague: Colleague)

    @Query("SELECT * FROM colleagues ORDER BY fullName ASC")
    fun getAllColleagues(): Flow<List<Colleague>>

    @Query("SELECT * FROM colleagues WHERE id = :id")
    fun getColleagueById(id: Int): Flow<Colleague>

    @Query("DELETE FROM colleagues WHERE id = :id")
    suspend fun delete(id: Int)
}
