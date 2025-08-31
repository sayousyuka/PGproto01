package com.example.pgproto01.viewmodel

class PunchLogViewModel {
}package com.example.pgproto01.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pgproto01.data.model.AppDatabase
import com.example.pgproto01.data.model.PunchLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PunchLogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val punchLogDao = db.punchLogDao()

    fun insert(log: PunchLog) {
        viewModelScope.launch(Dispatchers.IO) {
            punchLogDao.insert(log)
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
}
