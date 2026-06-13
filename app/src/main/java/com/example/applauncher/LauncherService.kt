package com.example.applauncher

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.applauncher.model.ExecutionLog
import com.example.applauncher.receiver.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class LauncherService : Service() {

    companion object {
        const val EXTRA_PACKAGE = "target_package"
        const val EXTRA_APP_NAME = "target_app_name"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra(EXTRA_PACKAGE)
        val appName = intent?.getStringExtra(EXTRA_APP_NAME)

        if (packageName != null) {
            handleAlarm(packageName, appName ?: packageName, startId)
        } else {
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    private fun handleAlarm(packageName: String, appName: String, startId: Int) {
        Log.d("LauncherService", "Alarm fired: $appName ($packageName)")

        val delayMs = Random.nextInt(1, 6) * 60_000L
        Log.d("LauncherService", "Random delay: ${delayMs / 1000}s")

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "AppLauncher:Alarm"
        )
        wakeLock.acquire(10 * 60_000L)

        CoroutineScope(Dispatchers.Main).launch {
            delay(delayMs)

            CoroutineScope(Dispatchers.IO).launch {
                (application as AppLauncherApp).logRepository.addLog(
                    ExecutionLog(packageName, "[闹钟触发] $appName", System.currentTimeMillis())
                )
            }

            if (!LauncherAccessibilityService.launchApp(packageName, appName)) {
                val bridgeIntent = Intent(this@LauncherService, BridgeActivity::class.java).apply {
                    putExtra(BridgeActivity.EXTRA_PACKAGE, packageName)
                    putExtra(BridgeActivity.EXTRA_APP_NAME, appName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    startActivity(bridgeIntent)
                } catch (e: Exception) {
                    Log.e("LauncherService", "startActivity failed", e)
                    CoroutineScope(Dispatchers.IO).launch {
                        (application as AppLauncherApp).logRepository.addLog(
                            ExecutionLog(packageName, "[启动失败] $appName", System.currentTimeMillis())
                        )
                    }
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                val app = application as AppLauncherApp
                val sched = app.scheduleRepository.schedule.first()
                if (sched != null && sched.enabled) {
                    AlarmReceiver.schedule(this@LauncherService, sched)
                }
            }

            wakeLock.release()
            stopSelf(startId)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
