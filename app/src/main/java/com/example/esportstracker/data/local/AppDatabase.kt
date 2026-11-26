package com.example.esportstracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.esportstracker.data.auth.UserDao
import com.example.esportstracker.data.auth.UserEntity
import com.example.esportstracker.data.favorites.FavoriteTeamDao
import com.example.esportstracker.data.favorites.FavoriteTeamEntity
import com.example.esportstracker.data.predictions.PredictionDao
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.data.reminders.ReminderDao
import com.example.esportstracker.data.reminders.ReminderEntity

@Database(
    entities = [
        UserEntity::class,
        FavoriteTeamEntity::class,
        PredictionEntity::class,
        ReminderEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteTeamDao(): FavoriteTeamDao
    abstract fun predictionDao(): PredictionDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        // Thêm cột lưu lựa chọn từng ván, giữ nguyên dữ liệu cũ
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE predictions ADD COLUMN gamePicks TEXT NOT NULL DEFAULT ''")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esports.db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instance = it }
            }
    }
}