package com.originalityshield.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.originalityshield.network.RetrofitClient
import com.originalityshield.network.Task
import kotlinx.coroutines.launch

@Composable
fun ArtistDashboard(navController: NavController, userId: Int) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        scope.launch {
            try {
                // The API now identifies the user from the Auth Token automatically
                tasks = RetrofitClient.api.getAssignments()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Artist Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Assignments:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(tasks) { task ->
                TaskItem(task) {
                    navController.navigate("canvas/${task.taskId}")
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.artworkTitle, style = MaterialTheme.typography.titleLarge)
            Text("Segment: ${task.segmentIndex}")
            Text("Status: ${task.status}")
        }
    }
}
