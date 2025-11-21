package com.rudra.smartworktracker.repository

import com.rudra.smartworktracker.data.dao.ColleagueDao
import com.rudra.smartworktracker.model.Colleague
import kotlinx.coroutines.flow.Flow

class ColleagueRepository(private val colleagueDao: ColleagueDao) {

    fun getAllColleagues(): Flow<List<Colleague>> = colleagueDao.getAllColleagues()

    fun getColleagueById(id: Int): Flow<Colleague> = colleagueDao.getColleagueById(id)

    suspend fun insert(colleague: Colleague) {
        colleagueDao.insert(colleague)
    }

    suspend fun update(colleague: Colleague) {
        colleagueDao.update(colleague)
    }

    suspend fun delete(id: Int) {
        colleagueDao.delete(id)
    }
}
