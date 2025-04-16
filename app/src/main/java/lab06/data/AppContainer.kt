package lab06.data

import android.content.Context

interface AppContainer {
    val todoTaskRepository: TodoTaskRepository
//    val currentDateProvider: CurrentDateProvider
//    val notificationHandler: NotificationHandler
//    val taskAlarmScheduler: TaskAlarmScheduler
}
class AppDataContainer(private val context: Context): AppContainer {
    override val todoTaskRepository: TodoTaskRepository by lazy {
        DatabaseTodoTaskRepository(AppDatabase.getInstance(context).taskDao())
    }

//    override val currentDateProvider: CurrentDateProvider by lazy {
//        SystemCurrentDateProvider()
//    }
//
//    override val notificationHandler: NotificationHandler by lazy {
//        NotificationHandler(context)
//    }
//
//    override val taskAlarmScheduler: TaskAlarmScheduler by lazy {
//        TaskAlarmScheduler(context)
//    }
}