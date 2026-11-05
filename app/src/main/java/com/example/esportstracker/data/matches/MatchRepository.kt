package com.example.esportstracker.data.matches

import com.example.esportstracker.data.remote.Network
import com.example.esportstracker.data.remote.PandaScoreApi

class MatchRepository(private val api: PandaScoreApi = Network.api) {
    suspend fun upcoming(game: Game) = api.upcoming(game.slug)
    suspend fun running(game: Game) = api.running(game.slug)
    suspend fun past(game: Game) = api.past(game.slug)
    suspend fun match(id: Long) = api.match(id)
    suspend fun team(id: Long) = api.team(id)
}