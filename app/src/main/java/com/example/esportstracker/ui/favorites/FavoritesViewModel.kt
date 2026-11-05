package com.example.esportstracker.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.data.favorites.FavoriteTeamEntity
import com.example.esportstracker.data.auth.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).favoriteTeamDao()
    private val userId = SessionManager(app).userId.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    val favorites: StateFlow<List<FavoriteTeamEntity>> = userId
        .flatMapLatest { dao.observeAll(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateNote(item: FavoriteTeamEntity, note: String) {
        viewModelScope.launch { dao.update(item.copy(note = note.trim())) }
    }

    fun remove(item: FavoriteTeamEntity) {
        viewModelScope.launch { dao.delete(item) }
    }
}