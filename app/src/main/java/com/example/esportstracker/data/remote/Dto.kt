package com.example.esportstracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    val id: Long,
    val name: String,
    val acronym: String? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class OpponentDto(val opponent: TeamDto)

@Serializable
data class ResultDto(
    @SerialName("team_id") val teamId: Long? = null,
    val score: Int? = null
)

@Serializable
data class LeagueDto(
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class MatchDto(
    val id: Long,
    val name: String,
    val status: String,
    @SerialName("begin_at") val beginAt: String? = null,
    @SerialName("winner_id") val winnerId: Long? = null,
    @SerialName("number_of_games") val numberOfGames: Int? = null,
    val tournament: TournamentDto? = null,
    val opponents: List<OpponentDto> = emptyList(),
    val results: List<ResultDto> = emptyList(),
    val league: LeagueDto? = null
)

@Serializable
data class PlayerDto(
    val id: Long,
    val name: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val role: String? = null,
    val nationality: String? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class TeamDetailDto(
    val id: Long,
    val name: String,
    val acronym: String? = null,
    val location: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("current_videogame") val game: VideogameDto? = null,
    val players: List<PlayerDto> = emptyList()
)

@Serializable
data class VideogameDto(
    val name: String? = null,
    val slug: String? = null
)
@Serializable
data class TournamentDto(
    val name: String? = null
)