package com.example.esportstracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.AppDatabase
import com.example.esportstracker.data.FavoriteTeamEntity
import com.example.esportstracker.data.MatchDto
import com.example.esportstracker.data.MatchRepository
import com.example.esportstracker.data.SessionManager
import com.example.esportstracker.data.TeamDetailDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.esportstracker.data.PredictionEntity
import com.example.esportstracker.data.ReminderEntity
import com.example.esportstracker.data.ReminderScheduler
import java.time.Instant

sealed interface DetailState<out T> {
    object Loading : DetailState<Nothing>
    data class Success<T>(val data: T) : DetailState<T>
    data class Error(val message: String) : DetailState<Nothing>
}

class MatchDetailViewModel(
    app: Application,
    handle: SavedStateHandle
) : AndroidViewModel(app) {
    private val matchId: Long = checkNotNull(handle["matchId"])
    private val repo = MatchRepository()
    private val predictionDao = AppDatabase.get(app).predictionDao()
    private val reminderDao = AppDatabase.get(app).reminderDao()
    private val userId = SessionManager(app).userId.filterNotNull()

    private val _state = MutableStateFlow<DetailState<MatchDto>>(DetailState.Loading)
    val state: StateFlow<DetailState<MatchDto>> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val prediction: StateFlow<PredictionEntity?> = userId
        .flatMapLatest { predictionDao.observeForMatch(it, matchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val reminder: StateFlow<ReminderEntity?> = userId
        .flatMapLatest { reminderDao.observeForMatch(it, matchId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            _state.value = try {
                DetailState.Success(repo.match(matchId))
            } catch (e: Exception) {
                DetailState.Error(e.message ?: "Lỗi mạng")
            }
        }
    }

    fun savePrediction(
        match: MatchDto,
        winnerId: Long,
        winnerName: String,
        score: String,
        note: String
    ) {
        viewModelScope.launch {
            val uid = userId.first()
            val old = predictionDao.find(uid, match.id)
            if (old == null) {
                predictionDao.insert(
                    PredictionEntity(
                        userId = uid,
                        matchId = match.id,
                        matchName = match.name,
                        leagueName = match.league?.name ?: "",
                        predictedWinnerId = winnerId,
                        predictedWinnerName = winnerName,
                        predictedScore = score.trim(),
                        note = note.trim()
                    )
                )
            } else {
                predictionDao.update(
                    old.copy(
                        predictedWinnerId = winnerId,
                        predictedWinnerName = winnerName,
                        predictedScore = score.trim(),
                        note = note.trim()
                    )
                )
            }
        }
    }

    fun deletePrediction() {
        viewModelScope.launch { predictionDao.deleteByMatch(userId.first(), matchId) }
    }

    fun saveReminder(match: MatchDto, minutes: Int) {
        val beginAt = match.beginAt ?: return
        viewModelScope.launch {
            val uid = userId.first()
            val time = Instant.parse(beginAt).toEpochMilli()
            val old = reminderDao.find(uid, match.id)
            val saved = if (old == null) {
                val r = ReminderEntity(
                    userId = uid,
                    matchId = match.id,
                    title = match.name,
                    matchTimeMillis = time,
                    minutesBefore = minutes
                )
                r.copy(id = reminderDao.insert(r))
            } else {
                val r = old.copy(matchTimeMillis = time, minutesBefore = minutes, enabled = true)
                reminderDao.update(r)
                r
            }
            ReminderScheduler.schedule(getApplication<Application>(), saved)
        }
    }

    fun deleteReminder() {
        viewModelScope.launch {
            val uid = userId.first()
            reminderDao.find(uid, matchId)?.let {
                reminderDao.delete(it)
                ReminderScheduler.cancel(getApplication<Application>(), it.id)
            }
        }
    }
}
class TeamDetailViewModel(
    app: Application,
    handle: SavedStateHandle
) : AndroidViewModel(app) {
    private val teamId: Long = checkNotNull(handle["teamId"])
    private val repo = MatchRepository()
    private val dao = AppDatabase.get(app).favoriteTeamDao()
    private val userId = SessionManager(app).userId.filterNotNull()

    private val _state = MutableStateFlow<DetailState<TeamDetailDto>>(DetailState.Loading)
    val state: StateFlow<DetailState<TeamDetailDto>> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = userId
        .flatMapLatest { dao.observeIsFavorite(it, teamId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            _state.value = try {
                DetailState.Success(repo.team(teamId))
            } catch (e: Exception) {
                DetailState.Error(e.message ?: "Lỗi mạng")
            }
        }
    }

    fun toggleFavorite(team: TeamDetailDto) {
        viewModelScope.launch {
            val uid = userId.first()
            if (dao.count(uid, team.id) > 0) {
                dao.deleteById(uid, team.id)
            } else {
                dao.insert(
                    FavoriteTeamEntity(
                        userId = uid,
                        teamId = team.id,
                        name = team.name,
                        logoUrl = team.imageUrl,
                        game = team.game?.name ?: ""
                    )
                )
            }
        }
    }
}