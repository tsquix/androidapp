package lab06.data

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as TodoApplication)
            ListViewModel(
                repository = application.container.todoTaskRepository,
                scheduler = application.container.taskAlarmScheduler
            )
        }
    }
}

fun CreationExtras.todoApplication(): TodoApplication {
    val app = this[APPLICATION_KEY]
    return app as TodoApplication
}