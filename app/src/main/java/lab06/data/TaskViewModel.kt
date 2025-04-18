package lab06.data
import android.widget.Toast
import lab06.data.TodoTaskRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lab06.TodoTask
import lab06.alarm.TaskAlarmScheduler

class TaskViewModel(
    private val repository: TodoTaskRepository,
    private val scheduler: TaskAlarmScheduler
) : ViewModel() {

    val tasks: StateFlow<List<TodoTask>> = repository.getAllAsStream().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    fun refreshAlarm() {
        viewModelScope.launch {
            // Use getAllAsStream() and collect the first value
            repository.getAllAsStream().collect { tasks ->
                val entities = tasks.map { TodoTaskEntity.fromModel(it) }
                scheduler.scheduleAlarmForNextTask(entities)
            }
        }
    }


    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.deleteItem(task)
            refreshAlarm()
            Toast.makeText(scheduler.context, "Task '${task.title}' deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
