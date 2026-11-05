package com.example.esportstracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.esportstracker.data.favorites.FavoriteTeamDao
import com.example.esportstracker.data.favorites.FavoriteTeamEntity
import com.example.esportstracker.data.predictions.PredictionDao
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.data.reminders.ReminderDao
import com.example.esportstracker.data.reminders.ReminderEntity
import com.example.esportstracker.data.auth.UserDao
import com.example.esportstracker.data.auth.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FavoriteTeamEntity::class,
        PredictionEntity::class,
        ReminderEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteTeamDao(): FavoriteTeamDao
    abstract fun predictionDao(): PredictionDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esports.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instance = it }
            }
    }
}