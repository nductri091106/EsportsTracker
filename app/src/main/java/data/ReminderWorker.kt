package com.example.esportstracker.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Sắp có trận đấu"
        val text = inputData.getString("text") ?: ""
        val id = inputData.getInt("notifId", 0)
        NotificationHelper.show(applicationContext, id, title, text)
        return Result.success()
    }
}