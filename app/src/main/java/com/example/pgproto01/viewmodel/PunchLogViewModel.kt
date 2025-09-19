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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow


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

    // ✅ ダイアログを開く
    fun openManualDialog(date: LocalDate, type: PunchType) {
        manualDialogDate = date
        manualDialogType = type
        isManualDialogVisible = true
    }

    // ✅ ダイアログを閉じる
    fun closeManualDialog() {
        isManualDialogVisible = false
    }

    // ✅ 手動打刻の保存
    fun insertManualPunch(staffId: String, dateTime: LocalDateTime, type: PunchType, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = PunchLog(
                staffId = staffId,
                timestamp = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                type = type.name,
                isManual = true,
                comment = comment  // ← これを追加
                // 💡 comment は PunchLog に存在しないので未使用！
            )
            punchLogDao.insert(log)
        }
    }


    // ✅ 既存の基本操作
    fun insert(log: PunchLog) {
        android.util.Log.d("PunchLogViewModel", "📥 insert呼ばれた: $log")
        viewModelScope.launch(Dispatchers.IO) {
            punchLogDao.insert(log)

            // 👇 保存後に全件取得してログ出力
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
