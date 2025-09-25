package com.example.pgproto01.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pgproto01.data.model.AppDatabase
import com.example.pgproto01.data.model.StaffEntity
import com.example.pgproto01.data.repository.StaffRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).staffDao()
    private val repo = StaffRepository(dao)

    // FlowをStateFlowに変換してUIで簡単に扱えるようにする
    val staffList: StateFlow<List<StaffEntity>> =
        repo.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addStaff(name: String, kana: String? = null) {
        viewModelScope.launch {
            repo.insert(StaffEntity(name = name, kana = kana))
        }
    }

    fun updateStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repo.update(staff)
        }
    }

    fun deleteStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repo.delete(staff)
        }
    }
}