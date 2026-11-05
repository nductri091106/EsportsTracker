package com.example.esportstracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["userId", "matchId"], unique = true)]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val matchId: Long,
    val title: String,
    val matchTimeMillis: Long,
    val minutesBefore: Int = 15,
    val enabled: Boolean = true
)

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(r: ReminderEntity): Long

    @Update
    suspend fun update(r: ReminderEntity)

    @Delete
    suspend fun delete(r: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE userId = :uid ORDER BY matchTimeMillis")
    fun observeAll(uid: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE userId = :uid AND matchId = :matchId LIMIT 1")
    fun observeForMatch(uid: Long, matchId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE userId = :uid AND matchId = :matchId LIMIT 1")
    suspend fun find(uid: Long, matchId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE userId = :uid")
    suspend fun getAll(uid: Long): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE userId = :uid")
    suspend fun deleteAll(uid: Long)
}