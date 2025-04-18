package lab06


import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.PrimaryKey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lab06.alarm.TaskAlarmScheduler
import lab06.data.AppContainer

import lab06.data.AppViewModelProvider
import lab06.data.FormViewModel
import lab06.data.ListViewModel
import lab06.data.LocalDateConverter
import lab06.data.NotificationBroadcastReceiver
import lab06.data.SettingsViewModel
import lab06.data.TodoApplication
import lab06.data.TodoTaskEntity
import lab06.data.TodoTaskForm
import pl.wsei.pam.lab01.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var container: AppContainer
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)
        container = (this.application as TodoApplication).container
        scheduleAlarm(this,2_000)
        enableEdgeToEdge()

        container = (this.application as TodoApplication).container
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

const val notificationID = 121
const val channelID = "Lab06 channel"
const val titleExtra = "title"
const val messageExtra = "message"
private fun createNotificationChannel(context: Context) {
    val name = "Lab06 channel"
    val descriptionText = "Lab06 is channel for notifications for approaching tasks."
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelID, name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun scheduleAlarm(context: Context, time: Long) {
    val intent = Intent(context, NotificationBroadcastReceiver::class.java)
    intent.putExtra(titleExtra, "Deadline")
    intent.putExtra(messageExtra, "Zbliża się termin zakończenia zadania")

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationID,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        time,
        pendingIntent
    )
}
fun scheduleTaskAlarm(context: Context, taskId: Int, title: String, deadline: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Ustaw alarm dzień przed terminem o tej samej godzinie
    val calendar = Calendar.getInstance().apply {
        timeInMillis = deadline
        add(Calendar.DAY_OF_YEAR, -1)
    }

    // Jeśli alarm w przeszłości, nie ustawiamy
    if (calendar.timeInMillis < System.currentTimeMillis()) return

    val intent = Intent(context, TaskAlarmScheduler::class.java).apply {
        putExtra("task_title", title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Alarm powtarzany co 4h
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_HOUR * 4,
        pendingIntent
    )
}


class NotificationHandler(private val context: Context) {
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)
    fun showSimpleNotification() {
        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle("Proste powiadomienie")
            .setContentText("Tekst powiadomienia")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationID, notification)
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    
    // Add notification permission handling
    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    
    LaunchedEffect(key1 = true) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }
    
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ListScreen(navController = navController)
        }
        composable("form") {
            FormScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = settingsViewModel)
        }
    }
}

@Composable
fun ListScreen(
    navController: NavController,
    viewModel: ListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by viewModel.listUiState.collectAsState()

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
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                items(items = listUiState.items, key = { it.id }) { item ->
                    ListItem(
                        item = item,
                        onDeleteClick = { viewModel.deleteTask(it) }  // Add this line
                    )
                }
            }
        }
    )
}




@Composable
fun FormScreen(
    navController: NavController,
    viewModel: FormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
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
                    viewModel.updateUiState(
                        TodoTaskForm(
                            title = taskTitle,
                            deadline = LocalDateConverter.toMillis(selectedDate),
                            isDone = taskDone,
                            priority = selectedPriority.name
                        )
                    )
                    coroutineScope.launch {
                        viewModel.save()
                        if (viewModel.todoTaskUiState.isValid) {
                            // Get the TaskAlarmScheduler instance
                            val taskAlarmScheduler = MainActivity.container.taskAlarmScheduler
                            // Schedule alarm for the new task
                            taskAlarmScheduler.scheduleAlarmForNextTask(
                                MainActivity.container.todoTaskRepository.getAllAsStream().first().map { task ->
                                    TodoTaskEntity(
                                        id = task.id,
                                        title = task.title,
                                        deadline = task.deadline,
                                        isDone = task.isDone,
                                        priority = Priority.valueOf(task.priority.name)
                                    )
                                }
                            )
                            
                            Toast.makeText(
                                context,
                                "Task saved and alarm set for: ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("list")
                        }
                    }
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
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.todoTaskUiState.titleError != null,
                    supportingText = {
                        if (viewModel.todoTaskUiState.titleError != null) {
                            Text(viewModel.todoTaskUiState.titleError!!)
                        }
                    }
                )

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

        
            }

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
        TodoTask(id = 1, title = "Programming", deadline = LocalDate.of(2024, 4, 18), isDone = false, priority = Priority.Low),
        TodoTask(id = 2, title = "Teaching", deadline = LocalDate.of(2024, 5, 12), isDone = false, priority = Priority.High),
        TodoTask(id = 3, title = "Learning", deadline = LocalDate.of(2024, 6, 28), isDone = true, priority = Priority.Low),
        TodoTask(id = 4, title = "Cooking", deadline = LocalDate.of(2024, 8, 18), isDone = false, priority = Priority.Medium),
    )
}

enum class Priority() {
    High, Medium, Low
}

data class TodoTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, //
    val title: String,
    val deadline: LocalDate,
    val isDone: Boolean,
    val priority: Priority
)
@Composable
fun ListItem(
    item: TodoTask, 
    modifier: Modifier = Modifier,
    onDeleteClick: (TodoTask) -> Unit = {}
) {
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

            Spacer(modifier = Modifier.weight(1f))
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

            IconButton(
                onClick = { onDeleteClick(item) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error
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
                IconButton(
                    onClick = { navController.navigate("settings") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopAppBar(
    navigateToSettings: () -> Unit,
    navController: NavController,
    viewModel: ListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    TopAppBar(
        title = { Text(text = "Todo App") },
        actions = {
            IconButton(onClick = navigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
fun TodoApp(
    navController: NavController,
    viewModel: ListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by viewModel.listUiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Todo App",
                showBackIcon = false,
                route = "list"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(items = listUiState.items, key = { it.id }) { task ->
                ListItem(
                    item = task,
                    onDeleteClick = { viewModel.deleteTask(it) }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Notification Settings",
                showBackIcon = true,
                route = "list"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Days before deadline", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.daysBeforeDeadline.toFloat(),
                onValueChange = { 
                    viewModel.updateSettings(settings.copy(daysBeforeDeadline = it.toInt()))
                },
                valueRange = 0f..7f,
                steps = 6
            )
            Text("${settings.daysBeforeDeadline} days")

            Text("Additional hours before deadline", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.hoursBeforeDeadline.toFloat(),
                onValueChange = { 
                    viewModel.updateSettings(settings.copy(hoursBeforeDeadline = it.toInt()))
                },
                valueRange = 0f..23f,
                steps = 22
            )
            Text("${settings.hoursBeforeDeadline} hours")

            Text("Number of reminders", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.repeatCount.toFloat(),
                onValueChange = { 
                    viewModel.updateSettings(settings.copy(repeatCount = it.toInt()))
                },
                valueRange = 1f..5f,
                steps = 3
            )
            Text("${settings.repeatCount} reminders")

            Text("Hours between reminders", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.repeatIntervalHours.toFloat(),
                onValueChange = { 
                    viewModel.updateSettings(settings.copy(repeatIntervalHours = it.toInt()))
                },
                valueRange = 1f..12f,
                steps = 10
            )
            Text("Every ${settings.repeatIntervalHours} hours")
        }
    }
}
