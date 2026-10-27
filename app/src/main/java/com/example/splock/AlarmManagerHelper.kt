package com.example.splock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object AlarmManagerHelper {

    fun scheduleAlarm(context: Context, alarm: AlarmItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_NAME", alarm.name)
            putExtra("ALARM_TONE", alarm.alarmTone)
            putExtra("ALARM_VOLUME", alarm.volume)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(alarm)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("AlarmHelper", "Alarm scheduled for: ${Calendar.getInstance().apply { timeInMillis = triggerTime }.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmHelper", "Missing exact alarm permission", e)
        }
    }

    fun cancelAlarm(context: Context, alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(alarm: AlarmItem): Long {
        val parts = alarm.time.split(":")
        if (parts.size != 2) return System.currentTimeMillis()

        var hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0

        if (alarm.amPm == "PM" && hour < 12) hour += 12
        if (alarm.amPm == "AM" && hour == 12) hour = 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Advanced: If repeatDays are set, find the next valid day
        if (alarm.repeatDays.isNotEmpty()) {
            val dayMap = mapOf(
                "Sun" to Calendar.SUNDAY,
                "Mon" to Calendar.MONDAY,
                "Tue" to Calendar.TUESDAY,
                "Wed" to Calendar.WEDNESDAY,
                "Thu" to Calendar.THURSDAY,
                "Fri" to Calendar.FRIDAY,
                "Sat" to Calendar.SATURDAY
            )
            
            var addedDays = 0
            while (addedDays < 7) {
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val isDayMatching = alarm.repeatDays.any { dayStr -> dayMap[dayStr] == currentDayOfWeek }
                
                if (isDayMatching) {
                    break
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                addedDays++
            }
        }

        return calendar.timeInMillis
    }
}
