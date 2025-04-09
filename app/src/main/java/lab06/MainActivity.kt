package lab06

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lab06.ui.theme.Lab01Theme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.wsei.pam.lab01.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab01Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Lab01Theme {
        Greeting("Android")
    }
}
@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    Lab01Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen()
        }
    }
}


@Composable
fun MainScreen(
    listScreen: @Composable (NavController) -> Unit = { ListScreen(it) }
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") { listScreen(navController) }
        composable("form") { FormScreen(navController) }
    }
}

@Composable
fun ListScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                content = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add task",
                        modifier = Modifier.scale(1.5f)
                    )
                },
                onClick = {
                    navController.navigate("form")
                }
            )
        },
        topBar = {
            AppTopBar(
                navController = navController,
                title = "List",
                showBackIcon = false,
                route = "form"
            )
        },
        content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(items = todoTasks()) { item ->
                    ListItem(item = item)
                }
            }
        }

    )
}



@Composable
fun FormScreen(navController: NavController) {
    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.Medium) }
    var taskDone by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Add New Task",
                showBackIcon = true,
                route = "list"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = {
                    // Here you'd typically save the task to your data source
                    val newTask = TodoTask(
                        title = taskTitle,
                        deadline = selectedDate,
                        isDone = taskDone,
                        priority = selectedPriority
                    )

                    // For demonstration purposes, just navigate back
                    navController.navigate("list")
                },
                content = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Save task",
                        modifier = Modifier.scale(1.5f)
                    )
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title input
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Priority selection
                Text(
                    text = "Task Priority",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Date selection
                Text(
                    text = "Task Deadline",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                }

                // Task status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = taskDone,
                        onCheckedChange = { taskDone = it }
                    )
                    Text(
                        text = "Task Completed",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Preview card
                Text(
                    text = "Task Preview",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    item = TodoTask(
                        title = if (taskTitle.isEmpty()) "Task Title" else taskTitle,
                        deadline = selectedDate,
                        isDone = taskDone,
                        priority = selectedPriority
                    )
                )
            }

            // Date picker dialog
            if (showDatePicker) {
                DateDialogPicker(
                    selectedDate = selectedDate,
                    onDateSelected = {
                        selectedDate = it
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDialogPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

fun todoTasks(): List<TodoTask> {
    return listOf(
        TodoTask("Programming", LocalDate.of(2024, 4, 18), false, Priority.Low),
        TodoTask("Teaching", LocalDate.of(2024, 5, 12), false, Priority.High),
        TodoTask("Learning", LocalDate.of(2024, 6, 28), true, Priority.Low),
        TodoTask("Cooking", LocalDate.of(2024, 8, 18), false, Priority.Medium),
    )
}

enum class Priority() {
    High, Medium, Low
}

data class TodoTask(
    val title: String,
    val deadline: LocalDate,
    val isDone: Boolean,
    val priority: Priority
)
@Composable
fun ListItem(item: TodoTask, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(120.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tytuł",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Priorytet",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "${item.priority}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Deadline",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${item.deadline}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Spacer to push the icon to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // Icon at the bottom center
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val iconRes = if (item.isDone) R.drawable.check else R.drawable.close
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = if (item.isDone) "Task completed" else "Task not completed",
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.navigate(route) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (route != "form") {
                OutlinedButton(
                    onClick = { navController.navigate("list") }
                ) {
                    Text(
                        text = "Zapisz",
                        fontSize = 18.sp
                    )
                }
            } else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                }
            }
        }
    )
}
