package com.example.splock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID") ?: return
        val alarmName = intent.getStringExtra("ALARM_NAME") ?: "Alarm"
        val alarmTone = intent.getStringExtra("ALARM_TONE") ?: "F1 Pit Siren"
        val alarmVolume = intent.getFloatExtra("ALARM_VOLUME", 0.8f)

        // Acquire a wakelock so the CPU doesn't sleep before the Activity starts
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Splock::AlarmReceiverWakeLock"
        )
        wakeLock.acquire(3000) // Hold for 3 seconds

        val triggerIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_NAME", alarmName)
            putExtra("ALARM_TONE", alarmTone)
            putExtra("ALARM_VOLUME", alarmVolume)
        }

        // Try to start the activity directly if we have overlay permission (allows popping up when screen is ON)
        if (android.provider.Settings.canDrawOverlays(context)) {
            try {
                context.startActivity(triggerIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
             // Fallback to start activity if the screen is off/locked
            try {
                context.startActivity(triggerIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use a new channel ID to avoid cached low-priority settings
        val channelId = "splock_alarm_channel_v3"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Splock High Priority Alarms",
                NotificationManager.IMPORTANCE_HIGH // Crucial for Full Screen Intents
            )
            channel.description = "Used to show full-screen alarms"
            channel.setBypassDnd(true)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Ringing")
            .setContentText(alarmName)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Crucial for Full Screen Intents
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(alarmId.hashCode(), notification)
    }
}
