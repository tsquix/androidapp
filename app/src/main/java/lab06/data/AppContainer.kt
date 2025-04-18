package lab06.data
import android.content.Context
import lab06.NotificationHandler
import lab06.alarm.TaskAlarmScheduler
import lab06.data.AppDatabase
import lab06.data.DatabaseTodoTaskRepository
import lab06.data.TodoTaskRepository


interface AppContainer {
    val todoTaskRepository: TodoTaskRepository
    val settingsRepository: SettingsRepository
    val taskAlarmScheduler: TaskAlarmScheduler
    val notificationHandler: NotificationHandler
    val currentDateProvider: CurrentDateProvider
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    override val todoTaskRepository: TodoTaskRepository by lazy {
        DatabaseTodoTaskRepository(AppDatabase.getInstance(context).taskDao())
    }

    override val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(context)
    }
    override val currentDateProvider: CurrentDateProvider by lazy {
        SystemCurrentDateProvider()
    }
    override val taskAlarmScheduler: TaskAlarmScheduler by lazy {
        TaskAlarmScheduler(context, settingsRepository)
    }
}

