package com.example.pgproto01.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: DailyComment)

    @Query("SELECT * FROM daily_comments WHERE staffId = :staffId AND date = :date LIMIT 1")
    fun getCommentForDay(staffId: String, date: String): Flow<DailyComment?>

    @Query("SELECT * FROM daily_comments WHERE staffId = :staffId")
    fun getAllComments(staffId: String): Flow<List<DailyComment>>
}
