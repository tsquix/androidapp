package lab06.alarm
import android.util.Log
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import lab06.*
import lab06.data.NotificationBroadcastReceiver
import java.time.*
import lab06.data.TodoTaskEntity
import android.widget.Toast
import lab06.data.NotificationSettings
import lab06.data.SettingsRepository

class TaskAlarmScheduler(
    val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private var currentAlarmTaskId: Int? = null
    private var currentAlarmTime: Long = 0L

    fun scheduleAlarmForNextTask(tasks: List<TodoTaskEntity>) {
        try {
            Log.d("TaskAlarm", "Starting alarm scheduling with ${tasks.size} tasks")
            
            val settings = settingsRepository.getSettings() ?: NotificationSettings() // Provide default settings if null
            
            val uncompletedTasks = tasks.filter {
                !it.isDone && !it.deadline.isBefore(LocalDate.now())
            }
    
            val nextTask = uncompletedTasks.minByOrNull { it.deadline } ?: run {
                cancelCurrentAlarm()
                return
            }

            val notificationTime = LocalDateTime.of(
                nextTask.deadline,  // LocalDate
                LocalTime.of(9, 0)
            )
                .minusDays(settings.daysBeforeDeadline.toLong())
                .minusHours(settings.hoursBeforeDeadline.toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            if (currentAlarmTaskId != nextTask.id || notificationTime < currentAlarmTime) {
                cancelCurrentAlarm()
                scheduleAlarm(notificationTime, nextTask.title)
                
                // Schedule repeat notifications if configured
                if (settings.repeatCount > 1) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    scheduleRepeatAlarms(alarmManager, notificationTime, nextTask.title, settings)
                }
                
                currentAlarmTaskId = nextTask.id
                currentAlarmTime = notificationTime
            }
        } catch (e: Exception) {
            Log.e("TaskAlarm", "Error scheduling alarm", e)
        }
    }

    private fun scheduleAlarm(time: Long, taskTitle: String) {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            putExtra(titleExtra, "Zadanie do wykonania")
            putExtra(messageExtra, "Jutro mija termin: '$taskTitle'")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
        val logMessage = "Alarm scheduled for task '$taskTitle' at $date"
        Log.d("TaskAlarm", logMessage)

        Toast.makeText(context, "Reminder set for: $taskTitle", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleRepeatAlarms(
        alarmManager: AlarmManager,
        baseTime: Long,
        title: String,
        settings: NotificationSettings
    ) {
        val interval = settings.repeatIntervalHours * 60 * 60 * 1000L // Convert hours to milliseconds
        for (i in 1 until settings.repeatCount) {
            val time = baseTime + interval * i
            val repeatIntent = PendingIntent.getBroadcast(
                context,
                notificationID + i,
                Intent(context, NotificationBroadcastReceiver::class.java).apply {
                    putExtra(titleExtra, "Przypomnienie")
                    putExtra(messageExtra, "Zbliża się termin: '$title'")
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, repeatIntent)

            val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
            val logMessage = "Repeat alarm #$i scheduled for '$title' at $date"
            Log.d("TaskAlarm", logMessage)
        }
    }

    private fun cancelCurrentAlarm() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val settings = settingsRepository.getSettings() ?: NotificationSettings()
            
            // Cancel all alarms including repeats
            val maxCancelCount = maxOf(settings.repeatCount, 1) // Ensure at least 1 alarm is cancelled
            for (i in 0 until maxCancelCount) {
                val intent = Intent(context, NotificationBroadcastReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationID + i,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                )
                pendingIntent?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                }
            }

            currentAlarmTaskId = null
            currentAlarmTime = 0L
        } catch (e: Exception) {
            Log.e("TaskAlarm", "Error cancelling alarm", e)
        }
    }
}
