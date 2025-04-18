package lab06.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import lab06.Priority
import lab06.TodoTask
import java.time.LocalDate

class FormViewModel(
    private val repository: TodoTaskRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {

    var todoTaskUiState by mutableStateOf(TodoTaskUiState())
        private set

    suspend fun save() {
        val validationResult = validate()
        todoTaskUiState = todoTaskUiState.copy(
            isValid = validationResult.isValid,
            titleError = validationResult.titleError,
            deadlineError = validationResult.deadlineError
        )
        
        if (validationResult.isValid) {
            repository.insertItem(todoTaskUiState.todoTask.toTodoTask())
        }
    }

    fun updateUiState(todoTaskForm: TodoTaskForm) {
        val validationResult = validate(todoTaskForm)
        todoTaskUiState = TodoTaskUiState(
            todoTask = todoTaskForm,
            isValid = validationResult.isValid,
            titleError = validationResult.titleError,
            deadlineError = validationResult.deadlineError
        )
    }

    private fun validate(uiState: TodoTaskForm = todoTaskUiState.todoTask): ValidationResult {
        val titleValid = uiState.title.isNotBlank()
        val deadlineValid = LocalDateConverter.fromMillis(uiState.deadline)
            .isAfter(currentDateProvider.currentDate)

        return ValidationResult(
            isValid = titleValid && deadlineValid,
            titleError = if (!titleValid) "Title cannot be empty" else null,
            deadlineError = if (!deadlineValid) "Deadline must be in the future" else null
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val titleError: String? = null,
        val deadlineError: String? = null
    )
}

data class TodoTaskUiState(
    var todoTask: TodoTaskForm = TodoTaskForm(),
    val isValid: Boolean = false,
    val titleError: String? = null,
    val deadlineError: String? = null
)

data class TodoTaskForm(
    val id: Int = 0,
    val title: String = "",
    val deadline: Long = LocalDateConverter.toMillis(LocalDate.now().plusDays(1)), // Add one day to current date
    val isDone: Boolean = false,
    val priority: String = Priority.Low.name
)

fun TodoTask.toTodoTaskUiState(isValid: Boolean = false): TodoTaskUiState = TodoTaskUiState(
    todoTask = this.toTodoTaskForm(),
    isValid = isValid
)

fun TodoTaskForm.toTodoTask(): TodoTask = TodoTask(
    id = id,
    title = title,
    deadline = LocalDateConverter.fromMillis(deadline),
    isDone = isDone,
    priority = Priority.valueOf(priority)
)

fun TodoTask.toTodoTaskForm(): TodoTaskForm = TodoTaskForm(
    id = id,
    title = title,
    deadline = LocalDateConverter.toMillis(deadline),
    isDone = isDone,
    priority = priority.name
)