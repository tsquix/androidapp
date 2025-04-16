package lab06.data

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lab06.TodoTask
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ListViewModel(val repository: TodoTaskRepository) : ViewModel() {

    // Inside your ListViewModel class
    fun saveTask(newTask: TodoTask) {
        viewModelScope.launch {
            // Call the repository's insert function to save the task to the database
            repository.insertItem(newTask)

            // If you need any post-save processing, you can do it here
            // For example, you might want to log the save operation
            Log.d("ListViewModel", "Task saved: ${newTask.title}")

            // Note: You don't need to update listUiState manually here
            // If you're using Room with Flow, the UI will automatically update
            // when the database changes, due to your StateFlow mapping in listUiState
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
}

data class ListUiState(val items: List<TodoTask> = listOf())