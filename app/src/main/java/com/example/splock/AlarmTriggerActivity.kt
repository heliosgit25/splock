package com.example.splock

import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splock.ui.theme.SplockTheme

class AlarmTriggerActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmId = intent.getStringExtra("ALARM_ID") ?: ""
        val alarmName = intent.getStringExtra("ALARM_NAME") ?: "Alarm"
        val alarmTone = intent.getStringExtra("ALARM_TONE") ?: "F1 Pit Siren"
        val alarmVolume = intent.getFloatExtra("ALARM_VOLUME", 0.8f)

        // Dismiss the notification since the user is seeing the activity
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId.hashCode())

        // Play Sound
        val resId = when (alarmTone) {
            "F1 Pit Siren" -> R.raw.f1_pit_siren
            "MotoGP Revving" -> R.raw.motogp_revving
            "Classic Buzzer" -> R.raw.classic_buzzer
            "Digital Beep" -> R.raw.digital_beep
            else -> R.raw.racing_airhorn
        }
        
        mediaPlayer = MediaPlayer.create(this, resId)?.apply {
            isLooping = true
            setVolume(alarmVolume, alarmVolume)
            start()
        }

        setContent {
            SplockTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ALARM RINGING",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = alarmName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { dismissAlarm() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.height(64.dp).width(140.dp)
                            ) {
                                Text("Dismiss", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun dismissAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
