import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lab06.Priority
import lab06.data.LocalDateConverter
import lab06.data.TodoTaskForm


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTaskInputForm(
    item: TodoTaskForm,
    modifier: Modifier = Modifier,
    onValueChange: (TodoTaskForm) -> Unit = {},
    enabled: Boolean = true
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Tytuł zadania")
        TextField(
            value = item.title,
            label = {Text("Dodaj tytuł zadania")},
            onValueChange = { onValueChange(item.copy(title = it)) },
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date picker
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            yearRange = 2000..2030,
            initialSelectedDateMillis = item.deadline,
        )
        var showDialog by remember { mutableStateOf(false) }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true },
            text = "Deadline: ${LocalDateConverter.fromMillis(item.deadline)}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )

        if (showDialog) {
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        datePickerState.selectedDateMillis?.let {
                            onValueChange(item.copy(deadline = it))
                        }
                    }) { Text("Pick") }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Czy zakończone?")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = item.isDone,
                onClick = { onValueChange(item.copy(isDone = true)) }
            )
            Text("Tak")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = !item.isDone,
                onClick = { onValueChange(item.copy(isDone = false)) }
            )
            Text("Nie")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Priorytet:")
        Priority.values().forEach { priority ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = item.priority == priority.name,
                    onClick = { onValueChange(item.copy(priority = priority.name)) }
                )
                Text(priority.name)
            }
        }
    }
}