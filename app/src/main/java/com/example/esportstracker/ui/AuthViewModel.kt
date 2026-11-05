package com.example.esportstracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.AppDatabase
import com.example.esportstracker.data.AuthRepository
import com.example.esportstracker.data.AuthResult
import com.example.esportstracker.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed interface SessionState {
    object Loading : SessionState
    object LoggedOut : SessionState
    data class LoggedIn(val userId: Long) : SessionState
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(
        AppDatabase.get(app).userDao(),
        SessionManager(app)
    )

    val session: StateFlow<SessionState> = repo.currentUserId
        .map { id -> if (id == null) SessionState.LoggedOut else SessionState.LoggedIn(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun clearError() { _error.value = null }

    fun login(username: String, password: String) =
        submit { repo.login(username, password) }

    fun register(username: String, password: String, displayName: String) =
        submit { repo.register(username, password, displayName) }

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }

    private fun submit(block: suspend () -> AuthResult) {
        viewModelScope.launch {
            _busy.value = true
            _error.value = null
            val result = block()
            if (result is AuthResult.Failure) _error.value = result.message
            _busy.value = false
        }
    }
}