package com.example.esportstracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.matches.Game
import com.example.esportstracker.data.remote.MatchDto
import com.example.esportstracker.data.matches.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading : UiState
    data class Success(val matches: List<MatchDto>) : UiState
    data class Error(val message: String) : UiState
}

class MatchListViewModel(
    private val repo: MatchRepository = MatchRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _game = MutableStateFlow(Game.LOL)
    val game: StateFlow<Game> = _game.asStateFlow()

    init { load() }

    fun selectGame(g: Game) {
        _game.value = g
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                val g = _game.value
                UiState.Success(repo.running(g) + repo.upcoming(g))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Lỗi mạng")
            }
        }
    }
}