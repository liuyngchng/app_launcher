# 定时启动器 (AppLauncher)

一个 Android 定时启动应用工具，可以在设定的时间自动打开指定的 App。

## 功能

- 选择任意已安装应用作为启动目标
- 支持每天两个时段（可分别设置不同时间）
- 按星期重复（周日~周六自由勾选）
- 一键启用/禁用定时任务
- 执行日志记录，可查看历史启动记录
- 开机自动恢复定时任务

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3
- **定时**：AlarmManager（精确闹钟）
- **存储**：DataStore Preferences + Gson
- **架构**：单 Activity + Repository 模式

## 权限说明

| 权限 | 用途 |
|------|------|
| `SCHEDULE_EXACT_ALARM` | 精确闹钟，保证按时启动应用 |
| `QUERY_ALL_PACKAGES` | 读取已安装应用列表供用户选择 |
| `RECEIVE_BOOT_COMPLETED` | 开机后重新注册闹钟 |
| `WAKE_LOCK` | 闹钟触发时短暂唤醒设备 |
| `DISABLE_KEYGUARD` | 在锁屏界面显示目标应用 |

## 构建

```bash
# 使用 Gradle Wrapper 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

## 使用

详见 [使用指南](./USAGE.md)。

### 快速上手

1. 选择要启动的目标应用
2. 设置启动时间（支持两个时段）
3. 勾选重复日期（周日~周六）
4. 打开「启用定时」开关并保存

首次使用需授予精确闹钟权限。

## 系统要求

- Android 12 (API 31) 及以上
