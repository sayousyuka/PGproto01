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

class PunchLogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val punchLogDao = db.punchLogDao()

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
    fun insertManualPunch(staffId: String, dateTime: LocalDateTime, type: PunchType, comment: String) {
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

    fun getPunchLogsForStaff(staffId: String): Flow<List<PunchLog>> {
        return punchLogDao.getByStaffId(staffId)
    }
}
