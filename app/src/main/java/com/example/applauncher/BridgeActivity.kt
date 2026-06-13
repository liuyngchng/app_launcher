package com.example.applauncher

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import com.example.applauncher.model.ExecutionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BridgeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Dismiss keyguard
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            val lock = km.newKeyguardLock("AppLauncher")
            @Suppress("DEPRECATION")
            lock.disableKeyguard()
        }

        // Acquire wake lock
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "AppLauncher:Bridge"
        )
        wakeLock.acquire(5000L)

        // Launch target app
        val packageName = intent.getStringExtra(EXTRA_PACKAGE) ?: run { finish(); return }
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: packageName
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            CoroutineScope(Dispatchers.IO).launch {
                AppLauncherApp.instance.logRepository.addLog(
                    ExecutionLog(packageName, appName, System.currentTimeMillis())
                )
            }
        }

        wakeLock.release()
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE = "target_package"
        const val EXTRA_APP_NAME = "target_app_name"
    }
}
