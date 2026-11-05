package com.example.esportstracker.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "predictions", primaryKeys = ["userId", "matchId"])
data class PredictionEntity(
    val userId: Long,
    val matchId: Long,
    val matchName: String,
    val leagueName: String,
    val predictedWinnerId: Long,
    val predictedWinnerName: String,
    val predictedScore: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PredictionDao {
    @Insert
    suspend fun insert(p: PredictionEntity)

    @Update
    suspend fun update(p: PredictionEntity)

    @Query("SELECT * FROM predictions WHERE userId = :uid ORDER BY createdAt DESC")
    fun observeAll(uid: Long): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE userId = :uid AND matchId = :matchId LIMIT 1")
    fun observeForMatch(uid: Long, matchId: Long): Flow<PredictionEntity?>

    @Query("SELECT * FROM predictions WHERE userId = :uid AND matchId = :matchId LIMIT 1")
    suspend fun find(uid: Long, matchId: Long): PredictionEntity?

    @Query("DELETE FROM predictions WHERE userId = :uid AND matchId = :matchId")
    suspend fun deleteByMatch(uid: Long, matchId: Long)

    @Query("DELETE FROM predictions WHERE userId = :uid")
    suspend fun deleteAll(uid: Long)
}