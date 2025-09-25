package com.example.pgproto01.data.repository

import com.example.pgproto01.data.model.StaffDao
import com.example.pgproto01.data.model.StaffEntity
import kotlinx.coroutines.flow.Flow

class StaffRepository(private val dao: StaffDao) {
    fun getAll(): Flow<List<StaffEntity>> = dao.getAll()
    suspend fun insert(staff: StaffEntity) = dao.insert(staff)
    suspend fun update(staff: StaffEntity) = dao.update(staff)
    suspend fun delete(staff: StaffEntity) = dao.delete(staff)
}