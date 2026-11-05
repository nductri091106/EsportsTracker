package com.example.esportstracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PandaScoreApi {
    @GET("{game}/matches/upcoming")
    suspend fun upcoming(
        @Path("game") game: String,
        @Query("page[size]") size: Int = 20
    ): List<MatchDto>

    @GET("{game}/matches/running")
    suspend fun running(@Path("game") game: String): List<MatchDto>

    @GET("{game}/matches/past")
    suspend fun past(
        @Path("game") game: String,
        @Query("page[size]") size: Int = 20,
        @Query("sort") sort: String = "-begin_at"
    ): List<MatchDto>

    @GET("matches/{id}")
    suspend fun match(@Path("id") id: Long): MatchDto

    @GET("teams/{id}")
    suspend fun team(@Path("id") id: Long): TeamDetailDto
}