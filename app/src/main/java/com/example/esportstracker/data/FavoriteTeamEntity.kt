package com.example.esportstracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorite_teams", primaryKeys = ["userId", "teamId"])
data class FavoriteTeamEntity(
    val userId: Long,
    val teamId: Long,
    val name: String,
    val logoUrl: String?,
    val game: String,
    val note: String = ""
)

@Dao
interface FavoriteTeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: FavoriteTeamEntity)

    @Query("SELECT * FROM favorite_teams WHERE userId = :uid ORDER BY name")
    fun observeAll(uid: Long): Flow<List<FavoriteTeamEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_teams WHERE userId = :uid AND teamId = :teamId)")
    fun observeIsFavorite(uid: Long, teamId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorite_teams WHERE userId = :uid AND teamId = :teamId")
    suspend fun count(uid: Long, teamId: Long): Int

    @Update
    suspend fun update(t: FavoriteTeamEntity)

    @Delete
    suspend fun delete(t: FavoriteTeamEntity)

    @Query("DELETE FROM favorite_teams WHERE userId = :uid AND teamId = :teamId")
    suspend fun deleteById(uid: Long, teamId: Long)

    @Query("DELETE FROM favorite_teams WHERE userId = :uid")
    suspend fun deleteAll(uid: Long)

}