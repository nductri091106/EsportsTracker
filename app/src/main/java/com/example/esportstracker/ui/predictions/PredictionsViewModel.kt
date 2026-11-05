package com.example.esportstracker.ui.predictions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.data.matches.MatchRepository
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.data.auth.SessionManager
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

data class PredictionItem(val prediction: PredictionEntity, val outcome: Outcome)

class PredictionsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).predictionDao()
    private val repo = MatchRepository()
    private val userId = SessionManager(app).userId.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val predictions: StateFlow<List<PredictionEntity>> = userId
        .flatMapLatest { dao.observeAll(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val outcomes = MutableStateFlow<Map<Long, Outcome>>(emptyMap())

    val items: StateFlow<List<PredictionItem>> =
        combine(predictions, outcomes) { list, map ->
            list.map { PredictionItem(it, map[it.matchId] ?: Outcome.CHECKING) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            predictions.collect { list ->
                list.filter { it.matchId !in outcomes.value }
                    .forEach { p -> launch { evaluate(p) } }
            }
        }
    }

    private suspend fun evaluate(p: PredictionEntity) {
        val outcome = try {
            val m = repo.match(p.matchId)
            when {
                m.status == "canceled" -> Outcome.UNKNOWN
                m.status != "finished" -> Outcome.PENDING
                m.winnerId == null -> Outcome.UNKNOWN
                m.winnerId == p.predictedWinnerId -> Outcome.CORRECT
                else -> Outcome.WRONG
            }
        } catch (e: Exception) {
            Outcome.UNKNOWN
        }
        outcomes.update { it + (p.matchId to outcome) }
    }

    fun refresh() {
        outcomes.value = emptyMap()
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