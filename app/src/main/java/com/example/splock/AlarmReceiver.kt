package com.example.splock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID") ?: return
        val alarmName = intent.getStringExtra("ALARM_NAME") ?: "Alarm"
        val alarmTone = intent.getStringExtra("ALARM_TONE") ?: "F1 Pit Siren"
        val alarmVolume = intent.getFloatExtra("ALARM_VOLUME", 0.8f)

        val triggerIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_NAME", alarmName)
            putExtra("ALARM_TONE", alarmTone)
            putExtra("ALARM_VOLUME", alarmVolume)
        }

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Ringing")
            .setContentText(alarmName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alarmId.hashCode(), notification)
    }
}
