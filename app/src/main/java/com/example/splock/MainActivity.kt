package com.example.splock

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.splock.ui.theme.SplockTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private enum class AppTab(val title: String) {
    SPORTS("Sports"),
    ALARM("Alarm"),
    WORLD_CLOCK("World Clock"),
    SETTINGS("Settings")
}

data class AlarmItem(
    val id: String = UUID.randomUUID().toString(),
    val time: String,          // e.g., "06:30"
    val amPm: String,          // "AM" or "PM"
    val name: String,          // e.g., "Monaco GP Lights Out"
    val enabled: Boolean = true,
    val repeatDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    val snoozeDuration: Int = 10, // in minutes
    val alarmTone: String = "F1 Pit Siren",
    val volume: Float = 0.8f
)

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle response
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(true) }

            SplockTheme(darkTheme = darkTheme) {
                var selectedTab by remember { mutableStateOf(AppTab.ALARM) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == AppTab.SPORTS,
                                onClick = { selectedTab = AppTab.SPORTS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Home,
                                        contentDescription = "Sports"
                                    )
                                },
                                label = { Text("Sports") }
                            )

                            NavigationBarItem(
                                selected = selectedTab == AppTab.ALARM,
                                onClick = { selectedTab = AppTab.ALARM },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Alarm,
                                        contentDescription = "Alarm"
                                    )
                                },
                                label = { Text("Alarm") }
                            )

                            NavigationBarItem(
                                selected = selectedTab == AppTab.WORLD_CLOCK,
                                onClick = { selectedTab = AppTab.WORLD_CLOCK },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Public,
                                        contentDescription = "World Clock"
                                    )
                                },
                                label = { Text("World Clock") }
                            )

                            NavigationBarItem(
                                selected = selectedTab == AppTab.SETTINGS,
                                onClick = { selectedTab = AppTab.SETTINGS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings") }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        AppTab.SPORTS -> SportsClockScreen(modifier = Modifier.padding(innerPadding))
                        AppTab.ALARM -> AlarmScreen(modifier = Modifier.padding(innerPadding))
                        AppTab.WORLD_CLOCK -> WorldClockScreen(modifier = Modifier.padding(innerPadding))
                        AppTab.SETTINGS -> SettingsScreen(
                            darkTheme = darkTheme,
                            onDarkThemeChange = { darkTheme = it },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SportsClockScreen(modifier: Modifier = Modifier) {
    val currentTime = remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = Date()
            delay(1000)
        }
    }

    val timeText = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentTime.value)
    val dateText = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(currentTime.value)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPLOCK MOTORSPORT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = timeText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            item {
                Text(
                    text = "NEXT RACE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Formula 1 • Monaco GP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "T-2h 45m",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Qualifying Session starts at 16:00 CET",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MotoGP • Italian GP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tomorrow",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Main Race starts at 14:00 CET",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var alarms by remember {
        mutableStateOf(
            listOf(
                AlarmItem(
                    time = "06:30",
                    amPm = "AM",
                    name = "Monaco GP Lights Out (T-30m)",
                    enabled = true,
                    repeatDays = listOf("Sat", "Sun"),
                    snoozeDuration = 10,
                    alarmTone = "F1 Pit Siren",
                    volume = 0.9f
                ),
                AlarmItem(
                    time = "12:00",
                    amPm = "PM",
                    name = "MotoGP Race Start Alert",
                    enabled = false,
                    repeatDays = listOf("Sun"),
                    snoozeDuration = 5,
                    alarmTone = "MotoGP Revving",
                    volume = 0.8f
                ),
                AlarmItem(
                    time = "07:00",
                    amPm = "AM",
                    name = "Wake up",
                    enabled = true,
                    repeatDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
                    snoozeDuration = 10,
                    alarmTone = "Classic Buzzer",
                    volume = 0.7f
                )
            )
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmItem?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALARMS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                item {
                    Text(
                        text = "YOUR ALARM SCHEDULE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(alarms, key = { it.id }) { alarm ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingAlarm = alarm
                                showDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = alarm.time,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = alarm.amPm,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alarm.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Repeat: ${if (alarm.repeatDays.isEmpty()) "Once" else alarm.repeatDays.joinToString(", ")} • Snooze: ${alarm.snoozeDuration}m",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = alarm.enabled,
                                onCheckedChange = { isChecked ->
                                    val updated = alarm.copy(enabled = isChecked)
                                    alarms = alarms.map { if (it.id == alarm.id) updated else it }
                                    if (isChecked) {
                                        AlarmManagerHelper.scheduleAlarm(context, updated)
                                    } else {
                                        AlarmManagerHelper.cancelAlarm(context, updated.id)
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }

            FloatingActionButton(
                onClick = {
                    editingAlarm = AlarmItem(
                        time = "08:00",
                        amPm = "AM",
                        name = "New Alarm",
                        enabled = true
                    )
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Alarm")
            }
        }
    }

    if (showDialog && editingAlarm != null) {
        AlarmEditDialog(
            alarm = editingAlarm!!,
            onDismiss = {
                showDialog = false
                editingAlarm = null
            },
            onSave = { updated ->
                val exists = alarms.any { it.id == updated.id }
                if (exists) {
                    alarms = alarms.map { if (it.id == updated.id) updated else it }
                } else {
                    alarms = alarms + updated
                }
                
                if (updated.enabled) {
                    AlarmManagerHelper.scheduleAlarm(context, updated)
                } else {
                    AlarmManagerHelper.cancelAlarm(context, updated.id)
                }

                showDialog = false
                editingAlarm = null
            },
            onDelete = { deletedId ->
                alarms = alarms.filter { it.id != deletedId }
                AlarmManagerHelper.cancelAlarm(context, deletedId)
                showDialog = false
                editingAlarm = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmEditDialog(
    alarm: AlarmItem,
    onDismiss: () -> Unit,
    onSave: (AlarmItem) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current

    var time by remember { mutableStateOf(alarm.time) }
    var amPm by remember { mutableStateOf(alarm.amPm) }
    var name by remember { mutableStateOf(alarm.name) }
    var snoozeDuration by remember { mutableStateOf(alarm.snoozeDuration) }
    var alarmTone by remember { mutableStateOf(alarm.alarmTone) }
    var volume by remember { mutableStateOf(alarm.volume) }
    var repeatDays by remember { mutableStateOf(alarm.repeatDays) }

    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val tones = listOf("F1 Pit Siren", "MotoGP Revving", "Classic Buzzer", "Digital Beep", "Racing Airhorn")
    val snoozeOptions = listOf(5, 10, 15, 20)

    var toneExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (alarm.name == "New Alarm") "Add Alarm" else "Edit Alarm", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(vertical = 4.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Alarm Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time (HH:MM)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = amPm,
                            onValueChange = { amPm = it },
                            label = { Text("AM/PM") },
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }

                item {
                    Text(text = "Repeat Days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allDays.forEach { day ->
                            val selected = repeatDays.contains(day)
                            Button(
                                onClick = {
                                    repeatDays = if (selected) {
                                        repeatDays - day
                                    } else {
                                        repeatDays + day
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(text = day, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(text = "Snooze Duration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        snoozeOptions.forEach { mins ->
                            val selected = snoozeDuration == mins
                            Button(
                                onClick = { snoozeDuration = mins },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(text = "${mins}m", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(text = "Alarm Tone & Preview", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = alarmTone,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toneExpanded = true },
                                label = { Text("Tone") }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { toneExpanded = true }
                            ) {}
                            androidx.compose.material3.DropdownMenu(
                                expanded = toneExpanded,
                                onDismissRequest = { toneExpanded = false }
                            ) {
                                tones.forEach { tone ->
                                    DropdownMenuItem(
                                        text = { Text(tone) },
                                        onClick = {
                                            alarmTone = tone
                                            toneExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val resId = when (alarmTone) {
                                    "F1 Pit Siren" -> R.raw.f1_pit_siren
                                    "MotoGP Revving" -> R.raw.motogp_revving
                                    "Classic Buzzer" -> R.raw.classic_buzzer
                                    "Digital Beep" -> R.raw.digital_beep
                                    else -> R.raw.racing_airhorn
                                }
                                MediaPlayer.create(context, resId)?.apply {
                                    setVolume(volume, volume)
                                    setOnCompletionListener { release() }
                                    start()
                                }
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Preview Tone")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview")
                        }
                    }
                }

                item {
                    Text(text = "Volume (${(volume * 100).toInt()})%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                        valueRange = 0f..1f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        alarm.copy(
                            time = time,
                            amPm = amPm,
                            name = name,
                            snoozeDuration = snoozeDuration,
                            alarmTone = alarmTone,
                            volume = volume,
                            repeatDays = repeatDays
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (alarm.name != "New Alarm") {
                    TextButton(
                        onClick = { onDelete(alarm.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun WorldClockScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "WORLD CLOCK & TRACK ZONES",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val venues = listOf(
                Triple("Monte Carlo", "Monaco (F1)", "+1 HR"),
                Triple("Mugello", "Italy (MotoGP)", "+2 HRS"),
                Triple("Silverstone", "United Kingdom", "+0 HRS"),
                Triple("Suzuka", "Japan", "+9 HRS"),
                Triple("Melbourne", "Australia", "+10 HRS")
            )

            items(venues) { (city, track, offset) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = city, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = track, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "14:45", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = offset, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var f1Notifications by remember { mutableStateOf(true) }
    var motogpNotifications by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SETTINGS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Text(
                    text = "MOTORSPORT PREFERENCES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Formula 1 Alerts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Qualifying, Race start, and podium alerts", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = f1Notifications,
                            onCheckedChange = { f1Notifications = it }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "MotoGP Alerts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Sprint, Main race, and lap record notifications", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = motogpNotifications,
                            onCheckedChange = { motogpNotifications = it }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "APP APPEARANCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Dark Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Optimized for night race viewing", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = onDarkThemeChange
                        )
                    }
                }
            }
        }
    }
}
