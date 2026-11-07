package com.example.esportstracker.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.data.auth.PasswordUtil
import com.example.esportstracker.data.reminders.ReminderScheduler
import com.example.esportstracker.data.auth.SessionManager
import com.example.esportstracker.data.auth.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val session = SessionManager(app)

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            val id = session.userId.filterNotNull().first()
            _user.value = db.userDao().findById(id)
        }
    }

    fun clearMessage() { _message.value = null }

    fun updateDisplayName(name: String) {
        val u = _user.value ?: return
        if (name.isBlank()) {
            _message.value = "Tên hiển thị không được để trống"
            return
        }
        viewModelScope.launch {
            val n = u.copy(displayName = name.trim())
            db.userDao().update(n)
            _user.value = n
            _message.value = "Đã cập nhật tên hiển thị"
        }
    }

    fun changePassword(old: String, new: String, confirm: String, onSuccess: () -> Unit) {
        val u = _user.value ?: return
        when {
            PasswordUtil.hash(old, u.salt) != u.passwordHash ->
                _message.value = "Mật khẩu hiện tại không đúng"
            new.length < 8 ->
                _message.value = "Mật khẩu mới tối thiểu 8 ký tự"
            new != confirm ->
                _message.value = "Mật khẩu nhập lại không khớp"
            else -> viewModelScope.launch {
                val salt = PasswordUtil.newSalt()
                val n = u.copy(passwordHash = PasswordUtil.hash(new, salt), salt = salt)
                db.userDao().update(n)
                _user.value = n
                _message.value = "Đã đổi mật khẩu"
                onSuccess()
            }
        }
    }

    fun deleteAccount() {
        val u = _user.value ?: return
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            db.reminderDao().getAll(u.id).forEach { ReminderScheduler.cancel(ctx, it.id) }
            db.reminderDao().deleteAll(u.id)
            db.predictionDao().deleteAll(u.id)
            db.favoriteTeamDao().deleteAll(u.id)
            db.userDao().delete(u)
            session.clear()
        }
    }
}