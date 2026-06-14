package com.example.applauncher.model

data class Diagnostics(
    val exactAlarmGranted: Boolean,
    val batteryWhitelisted: Boolean,
    val hasLockScreen: Boolean,
    val targetAppInstalled: Boolean,
    val targetAppName: String,
    val alarmsRegistered: Boolean,
    val scheduleEnabled: Boolean,
    val hasSchedule: Boolean,
    val accessibilityEnabled: Boolean,
    val overlayGranted: Boolean
) {
    val allOk: Boolean
        get() = exactAlarmGranted && batteryWhitelisted && !hasLockScreen
                && targetAppInstalled && alarmsRegistered
                && scheduleEnabled
}
