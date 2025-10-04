package com.example.pgproto01.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pgproto01.data.model.AppDatabase
import com.example.pgproto01.data.model.PunchLog
import com.example.pgproto01.data.model.PunchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import com.example.pgproto01.data.model.DailyComment
import java.time.format.DateTimeFormatter // ← これを追加
import com.example.pgproto01.data.model.toRequest
import com.example.pgproto01.data.remote.ApiClient

import android.util.Log

class PunchLogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val punchLogDao = db.punchLogDao()
    private val dailyCommentDao = db.dailyCommentDao()

    private val _missingPunches = mutableStateOf<List<PunchType>>(emptyList())
    val missingPunches: List<PunchType> get() = _missingPunches.value

    // ✅ ダイアログ制御用ステート
    var isManualDialogVisible by mutableStateOf(false)
        private set

    var manualDialogDate by mutableStateOf<LocalDate?>(null)
        private set

    var manualDialogType by mutableStateOf<PunchType?>(null)
        private set

    // ✅ 打刻ボタンの pendingType を管理
    var pendingType by mutableStateOf<PunchType?>(null)
        private set

    fun updatePendingType(type: PunchType?) {
        pendingType = type
    }

    // ✅ 手動ダイアログ制御
    fun openManualDialog(date: LocalDate, type: PunchType) {
        manualDialogDate = date
        manualDialogType = type
        isManualDialogVisible = true
    }

    fun closeManualDialog() {
        isManualDialogVisible = false
        manualDialogDate = null
        manualDialogType = null
    }

    // ✅ 手動打刻の保存
    fun insertManualPunch(staffId: Long, dateTime: LocalDateTime, type: PunchType, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = PunchLog(
                staffId = staffId,
                timestamp = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                type = type.name,
                isManual = true,
                comment = comment
            )
            punchLogDao.insert(log)
        }
    }

    // ✅ DB基本操作
    fun insert(log: PunchLog) {
        android.util.Log.i("PunchLogViewModel", "📥 insert呼ばれた: $log")
        viewModelScope.launch(Dispatchers.IO) {
            punchLogDao.insert(log)
            val all = punchLogDao.getAll()
            android.util.Log.d("PunchLogViewModel", "現在のDB内容: $all")
        }
    }

    fun delete(log: PunchLog) {
        viewModelScope.launch(Dispatchers.IO) {
            punchLogDao.delete(log)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            punchLogDao.clearAll()
        }
    }

    fun getPunchLogsForStaff(staffId: Long): Flow<List<PunchLog>> {
        return punchLogDao.getByStaffId(staffId)
    }
    // Flow を State に変換して UI で監視
    fun getCommentForDay(staffId: Long, date: LocalDate): Flow<DailyComment?> {
        return dailyCommentDao.getCommentForDay(staffId.toString(), date.toString())
    }

    fun setComment(staffId: Long, date: LocalDate, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dailyCommentDao.insert(
                DailyComment(staffId.toString(), date.toString(), text)
            )
        }
    }
    fun updateMissingPunches(missing: List<PunchType>) {
        _missingPunches.value = missing
    }

    private val _showManualDialog = mutableStateOf(false)
    val showManualDialog: Boolean get() = _showManualDialog.value

    fun updateShowManualDialog(show: Boolean) {
        _showManualDialog.value = show
    }
    fun insertPunchLog(
        staffId: Long,
        type: PunchType,
        timestamp: LocalDateTime,
        isManual: Boolean = false
    ) {
        val log = PunchLog(
            staffId = staffId,
            timestamp = timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = type.name,
            isManual = isManual,
            isDeleted = false,
            comment = null,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            punchLogDao.insert(log)

            // ✅ Lambda送信も追加
            val request = log.toRequest()
            val success = ApiClient.postAttendanceLog(
                apiUrl = "https://mcd3aliox3.execute-api.ap-southeast-2.amazonaws.com/attendance",
                log = request
            )

            if (success) {
                Log.i("PunchLogViewModel", "✅ 勤怠ログ送信成功")
            } else {
                Log.e("PunchLogViewModel", "❌ 勤怠ログ送信失敗")
            }

        }
    }

    fun getMissingPunchesForType(
        logs: List<PunchLog>,
        type: PunchType
    ): List<PunchType> {
        val typesToday = logs.map { it.type }.toSet()

        return when (type) {
            PunchType.IN -> emptyList()  // 出勤なら補完不要
            PunchType.BREAK_OUT -> if (!typesToday.contains(PunchType.IN.name)) listOf(PunchType.IN) else emptyList()
            PunchType.BREAK_IN -> {
                val missing = mutableListOf<PunchType>()
                if (!typesToday.contains(PunchType.IN.name)) missing.add(PunchType.IN)
                if (!typesToday.contains(PunchType.BREAK_OUT.name)) missing.add(PunchType.BREAK_OUT)
                missing
            }
            PunchType.OUT -> {
                val missing = mutableListOf<PunchType>()
                if (!typesToday.contains(PunchType.IN.name)) missing.add(PunchType.IN)
                if (typesToday.contains(PunchType.BREAK_OUT.name) && !typesToday.contains(PunchType.BREAK_IN.name)) {
                    missing.add(PunchType.BREAK_IN)
                }
                missing
            }
        }
    }

}
