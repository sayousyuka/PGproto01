package com.example.pgproto01.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PunchLog::class,
        DailyComment::class,
        StaffEntity::class       // ← ★ 追加
    ],
    version = 6, // ← 1つバージョンを上げる（今 3 なら 4 に）
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun punchLogDao(): PunchLogDao
    abstract fun dailyCommentDao(): DailyCommentDao // ← 追加
    abstract fun staffDao(): StaffDao    // ← ★ 追加

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ★ Migration定義（旧テーブルを新しい構造に作り直す）
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 新しいテーブル作成
                database.execSQL("""
                    CREATE TABLE punch_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        staffId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        isManual INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL,
                        comment TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """)

                // 2. データ移行（今回は旧データ破棄でOK）
                //    必要なら SELECT で旧カラムから新カラムへ変換コピーも可能

                // 3. 古いテーブル削除
                database.execSQL("DROP TABLE punch_logs")

                // 4. 新しいテーブルをリネーム
                database.execSQL("ALTER TABLE punch_logs_new RENAME TO punch_logs")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "punch_log_database"
                )
                    .fallbackToDestructiveMigration() // ← ここを追加
                    //.addMigrations(MIGRATION_1_2)    // 開発中はコメントアウトしてOK
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
