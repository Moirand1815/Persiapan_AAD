package com.dicoding.habitapp.notification

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.ui.countdown.CountDownActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)

    private fun getPendingIntent(habitId: Int): PendingIntent? {
        val intent = Intent(applicationContext, CountDownActivity::class.java).apply {
            putExtra(HABIT_ID, habitId)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    override fun doWork(): Result {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify = prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        //TODO 12 : If notification preference on, show notification with pending intent
        if (shouldNotify){
            val pendingIntent = getPendingIntent(habitId)
            val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(habitTitle)
                        .setContentText(String.format(applicationContext.getString(R.string.notify_content)))
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)


            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationBuilder.let {
                notificationManager.notify(habitId, it.build())
            }
        }
        return Result.success()
    }
}
