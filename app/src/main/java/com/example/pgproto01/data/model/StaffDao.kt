package com.example.pgproto01.data.model

import androidx.room.*
import com.example.pgproto01.data.model.StaffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    // 全件取得（リアルタイムに監視できるよう Flow を返す）
    @Query("SELECT * FROM staffs ORDER BY id ASC")
    fun getAll(): Flow<List<StaffEntity>>

    // 1件挿入
    @Insert
    suspend fun insert(staff: StaffEntity)

    // 既存データ更新
    @Update
    suspend fun update(staff: StaffEntity)

    // 削除
    @Delete
    suspend fun delete(staff: StaffEntity)
}