package com.example.esportstracker.ui.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.auth.SessionManager
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.data.reminders.ReminderEntity
import com.example.esportstracker.data.reminders.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).reminderDao()
    private val userId = SessionManager(app).userId.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reminders: StateFlow<List<ReminderEntity>> = userId
        .flatMapLatest { dao.observeAll(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMinutes(r: ReminderEntity, minutes: Int) {
        viewModelScope.launch {
            val n = r.copy(minutesBefore = minutes)
            dao.update(n)
            ReminderScheduler.schedule(getApplication<Application>(), n)
        }
    }

    fun setEnabled(r: ReminderEntity, enabled: Boolean) {
        viewModelScope.launch {
            val n = r.copy(enabled = enabled)
            dao.update(n)
            ReminderScheduler.schedule(getApplication<Application>(), n)
        }
    }

    fun remove(r: ReminderEntity) {
        viewModelScope.launch {
            dao.delete(r)
            ReminderScheduler.cancel(getApplication<Application>(), r.id)
        }
    }

    fun sendTest() {
        ReminderScheduler.test(getApplication<Application>())
    }
}