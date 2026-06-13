package com.example.applauncher

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.applauncher.receiver.AlarmReceiver
import com.example.applauncher.ui.screens.HomeScreen
import com.example.applauncher.ui.theme.AppLauncherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestExactAlarmLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check and re-schedule after returning from settings
        scheduleIfReady()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AppLauncherApp

        setContent {
            AppLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val schedule by app.scheduleRepository.schedule.collectAsState(
                        initial = null
                    )
                    HomeScreen(
                        currentSchedule = schedule,
                        onSave = { sched ->
                            CoroutineScope(Dispatchers.IO).launch {
                                app.scheduleRepository.save(sched)
                            }
                            scheduleAlarms(sched)
                            android.widget.Toast.makeText(this@MainActivity, "设置已保存", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onToggleEnabled = { enabled ->
                            CoroutineScope(Dispatchers.IO).launch {
                                app.scheduleRepository.setEnabled(enabled)
                                val sched = app.scheduleRepository.schedule.first()
                                if (enabled && sched != null) {
                                    scheduleAlarms(sched)
                                } else {
                                    AlarmReceiver.cancel(this@MainActivity)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scheduleIfReady()
    }

    private fun scheduleIfReady() {
        if (!hasExactAlarmPermission()) return
        CoroutineScope(Dispatchers.IO).launch {
            val app = application as AppLauncherApp
            val sched = app.scheduleRepository.schedule.first()
            if (sched != null && sched.enabled) {
                scheduleAlarms(sched)
            }
        }
    }

    private fun scheduleAlarms(schedule: com.example.applauncher.model.Schedule) {
        if (!hasExactAlarmPermission()) {
            requestExactAlarmPermission()
            return
        }
        AlarmReceiver.schedule(this, schedule)
    }

    private fun hasExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            requestExactAlarmLauncher.launch(intent)
        }
    }
}
