package com.rd.applauncher

import android.app.Application
import com.rd.applauncher.data.LogRepository
import com.rd.applauncher.data.ScheduleRepository

class AppLauncherApp : Application() {

    lateinit var scheduleRepository: ScheduleRepository
        private set
    lateinit var logRepository: LogRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        scheduleRepository = ScheduleRepository(this)
        logRepository = LogRepository(this)
    }

    companion object {
        lateinit var instance: AppLauncherApp
            private set
    }
}
