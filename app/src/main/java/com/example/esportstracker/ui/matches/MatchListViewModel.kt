package com.example.esportstracker.ui.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.matches.Game
import com.example.esportstracker.data.matches.MatchRepository
import com.example.esportstracker.data.remote.MatchDto
import kotlinx.coroutines.CancellationException
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
                    MatchFilter.RESULTS -> loadResults(g)
                }
                UiState.Success(list)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Lỗi mạng")
            }
        }
    }

    private suspend fun loadResults(g: Game): List<MatchDto> {
        // 1) Nhờ API lọc sẵn các trận đã kết thúc
        val finished = repo.past(g, 1, "finished")
        if (finished.isNotEmpty()) return finished

        // 2) Không có: thử lấy tất cả rồi bỏ trận hủy/hoãn
        val all = repo.past(g, 1)
        val usable = all.filter { it.status != "canceled" && it.status != "postponed" }
        if (usable.isNotEmpty()) return usable

        // 3) Vẫn không có: báo rõ nguyên nhân (tạm thời, để tìm lỗi)
        return emptyList()
    }
}