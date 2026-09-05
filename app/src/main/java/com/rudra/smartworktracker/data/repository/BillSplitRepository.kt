package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.BillSplitDao
import com.rudra.smartworktracker.data.entity.BillSplit
import kotlinx.coroutines.flow.Flow

class BillSplitRepository(
    private val dao: BillSplitDao
) {
    fun getAll(): Flow<List<BillSplit>> = dao.getAll()

    suspend fun getById(id: Long): BillSplit? = dao.getById(id)

    suspend fun insert(billSplit: BillSplit): Long = dao.insert(billSplit)

    suspend fun update(billSplit: BillSplit) = dao.update(billSplit)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
