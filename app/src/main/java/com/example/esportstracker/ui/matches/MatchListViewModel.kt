package com.example.esportstracker.ui.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.matches.Game
import com.example.esportstracker.data.matches.MatchRepository
import com.example.esportstracker.data.remote.MatchDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading : UiState
    data class Success(val matches: List<MatchDto>) : UiState
    data class Error(val message: String) : UiState
}

enum class MatchFilter(val label: String) {
    UPCOMING("Sắp tới"),
    RESULTS("Kết quả")
}

class MatchListViewModel(
    private val repo: MatchRepository = MatchRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _game = MutableStateFlow(Game.LOL)
    val game: StateFlow<Game> = _game.asStateFlow()

    private val _filter = MutableStateFlow(MatchFilter.UPCOMING)
    val filter: StateFlow<MatchFilter> = _filter.asStateFlow()

    private var job: Job? = null

    init { load() }

    fun selectGame(g: Game) {
        _game.value = g
        load()
    }

    fun selectFilter(f: MatchFilter) {
        _filter.value = f
        load()
    }

    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                val g = _game.value
                val list = when (_filter.value) {
                    MatchFilter.UPCOMING -> repo.running(g) + repo.upcoming(g)
                    MatchFilter.RESULTS -> repo.past(g)
                }
                UiState.Success(list)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Lỗi mạng")
            }
        }
    }
}