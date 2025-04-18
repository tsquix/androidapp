package lab06.data

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lab06.TodoTask
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lab06.alarm.TaskAlarmScheduler

class ListViewModel(
    val repository: TodoTaskRepository,
    private val scheduler: TaskAlarmScheduler
) : ViewModel() {

    fun saveTask(newTask: TodoTask) {
        viewModelScope.launch {
            repository.insertItem(newTask)
            Log.d("ListViewModel", "Task saved: ${newTask.title}")

            val tasks = repository.getAllAsStream().first()
            val entities = tasks.map { TodoTaskEntity.fromModel(it) }
            scheduler.scheduleAlarmForNextTask(entities)
        }
    }
    val listUiState: StateFlow<ListUiState>
        get() {
            return repository.getAllAsStream().map { ListUiState(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = ListUiState()
                )
        }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.deleteItem(task)
            Log.d("ListViewModel", "Task deleted: ${task.title}")
        }
    }
}

data class ListUiState(val items: List<TodoTask> = listOf())