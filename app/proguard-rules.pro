# Kotlin metadata — required for reflection and data classes
-keepattributes *Annotation*
-keepattributes Signature

# Keep data classes serialized with Gson
-keepclassmembers class com.rd.applauncher.model.ExecutionLog {
    <fields>;
}
-keep class com.rd.applauncher.model.ExecutionLog

# Keep Application class (referenced in manifest by name)
-keep class com.rd.applauncher.AppLauncherApp

# Keep receivers — instantiated by the system via manifest
-keep class com.rd.applauncher.receiver.** { *; }

# Gson
-keepattributes EnclosingMethod
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
