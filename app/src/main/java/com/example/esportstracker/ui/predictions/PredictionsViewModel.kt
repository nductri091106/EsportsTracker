package com.example.esportstracker.ui.predictions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.auth.SessionManager
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.data.matches.MatchRepository
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.ui.matches.formatDate
import com.example.esportstracker.ui.matches.formatTime
import com.example.esportstracker.ui.matches.matchTitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Outcome { CHECKING, PENDING, CORRECT, WRONG, UNKNOWN }

data class MatchInfo(
    val title: String,
    val league: String,
    val date: String,
    val score: String?
)

data class Evaluation(val outcome: Outcome, val info: MatchInfo?)

data class PredictionItem(
    val prediction: PredictionEntity,
    val outcome: Outcome,
    val info: MatchInfo?
)

class PredictionsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).predictionDao()
    private val repo = MatchRepository()
    private val userId = SessionManager(app).userId.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val predictions: StateFlow<List<PredictionEntity>> = userId
        .flatMapLatest { dao.observeAll(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val evaluations = MutableStateFlow<Map<Long, Evaluation>>(emptyMap())

    val items: StateFlow<List<PredictionItem>> =
        combine(predictions, evaluations) { list, map ->
            list.map { p ->
                val e = map[p.matchId]
                PredictionItem(p, e?.outcome ?: Outcome.CHECKING, e?.info)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            predictions.collect { list ->
                list.filter { it.matchId !in evaluations.value }
                    .forEach { p -> launch { evaluate(p) } }
            }
        }
    }

    private suspend fun evaluate(p: PredictionEntity) {
        val result = try {
            val m = repo.match(p.matchId)
            val teamA = m.opponents.getOrNull(0)?.opponent
            val teamB = m.opponents.getOrNull(1)?.opponent
            val sa = m.results.firstOrNull { it.teamId == teamA?.id }?.score
            val sb = m.results.firstOrNull { it.teamId == teamB?.id }?.score
            val finished = m.status == "finished"
            val score =
                if (finished && sa != null && sb != null && !(sa == 0 && sb == 0)) "$sa - $sb" else null
            val info = MatchInfo(
                title = matchTitle(m),
                league = m.league?.name ?: p.leagueName,
                date = if (finished) formatDate(m.beginAt) ?: "" else formatTime(m.beginAt),
                score = score
            )
            val outcome = when {
                m.status == "canceled" -> Outcome.UNKNOWN
                !finished -> Outcome.PENDING
                m.winnerId == null -> Outcome.UNKNOWN
                m.winnerId == p.predictedWinnerId -> Outcome.CORRECT
                else -> Outcome.WRONG
            }
            Evaluation(outcome, info)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Evaluation(Outcome.UNKNOWN, null)
        }
        evaluations.update { it + (p.matchId to result) }
    }

    fun refresh() {
        evaluations.value = emptyMap()
        viewModelScope.launch {
            predictions.value.forEach { p -> launch { evaluate(p) } }
        }
    }

    fun remove(p: PredictionEntity) {
        viewModelScope.launch {
            dao.deleteByMatch(userId.first(), p.matchId)
        }
    }
}