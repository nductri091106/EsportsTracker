package com.example.esportstracker.data.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private fun workName(id: Long) = "reminder_$id"

    fun schedule(context: Context, r: ReminderEntity) {
        val triggerAt = r.matchTimeMillis - r.minutesBefore * 60_000L
        val delay = triggerAt - System.currentTimeMillis()
        if (!r.enabled || delay <= 0) {
            cancel(context, r.id)
            return
        }
        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "title" to "Sắp diễn ra: ${r.title}",
                    "text" to "Trận đấu bắt đầu sau ${r.minutesBefore} phút",
                    "notifId" to r.id.toInt()
                )
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(r.id), ExistingWorkPolicy.REPLACE, work)
    }

    fun cancel(context: Context, id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
    }

    // Thông báo thử sau 10 giây, để kiểm tra và demo
    fun test(context: Context) {
        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    "title" to "Thông báo thử",
                    "text" to "Nhắc lịch hoạt động bình thường",
                    "notifId" to 999999
                )
            )
            .build()
        WorkManager.getInstance(context).enqueue(work)
    }
}