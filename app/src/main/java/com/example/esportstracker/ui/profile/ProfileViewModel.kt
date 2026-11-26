package com.example.esportstracker.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.auth.PasswordUtil
import com.example.esportstracker.data.auth.SessionManager
import com.example.esportstracker.data.auth.UserEntity
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.data.reminders.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import com.example.esportstracker.data.auth.AvatarStore

data class ProfileStats(
    val favorites: Int = 0,
    val predictions: Int = 0,
    val reminders: Int = 0
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val session = SessionManager(app)
    private val userId = session.userId.filterNotNull()

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: StateFlow<ProfileStats> = userId
        .flatMapLatest { uid ->
            combine(
                db.favoriteTeamDao().observeAll(uid),
                db.predictionDao().observeAll(uid),
                db.reminderDao().observeAll(uid)
            ) { f, p, r -> ProfileStats(f.size, p.size, r.size) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileStats())

    init {
        viewModelScope.launch {
            _user.value = db.userDao().findById(userId.first())
        }
    }

    fun clearMessage() { _message.value = null }

    /** onResult: null nếu thành công, ngược lại là nội dung lỗi. */
    fun updateDisplayName(name: String, onResult: (String?) -> Unit) {
        val u = _user.value ?: return
        if (name.isBlank()) {
            onResult("Tên hiển thị không được để trống.")
            return
        }
        viewModelScope.launch {
            val n = u.copy(displayName = name.trim())
            db.userDao().update(n)
            _user.value = n
            _message.value = "Đã cập nhật tên hiển thị"
            onResult(null)
        }
    }

    fun changePassword(old: String, new: String, confirm: String, onResult: (String?) -> Unit) {
        val u = _user.value ?: return
        val error = when {
            PasswordUtil.hash(old, u.salt) != u.passwordHash -> "Mật khẩu hiện tại không đúng."
            new.length < 8 -> "Mật khẩu mới tối thiểu 8 ký tự."
            new != confirm -> "Mật khẩu nhập lại không khớp."
            else -> null
        }
        if (error != null) {
            onResult(error)
            return
        }
        viewModelScope.launch {
            val salt = PasswordUtil.newSalt()
            val n = u.copy(passwordHash = PasswordUtil.hash(new, salt), salt = salt)
            db.userDao().update(n)
            _user.value = n
            _message.value = "Đã đổi mật khẩu"
            onResult(null)
        }
    }

    fun setAvatar(uri: Uri) {
        viewModelScope.launch {
            val ok = AvatarStore.save(getApplication<Application>(), userId.first(), uri)
            _message.value = if (ok) "Đã cập nhật ảnh đại diện" else "Không thể dùng ảnh này"
        }
    }

    fun removeAvatar() {
        viewModelScope.launch {
            AvatarStore.delete(getApplication<Application>(), userId.first())
            _message.value = "Đã xóa ảnh đại diện"
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
            AvatarStore.delete(ctx, u.id)
            db.userDao().delete(u)
            session.clear()
        }
    }
}