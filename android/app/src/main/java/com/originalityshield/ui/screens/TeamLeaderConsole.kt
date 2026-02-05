package com.originalityshield.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.originalityshield.network.RetrofitClient
import com.originalityshield.network.Task
import kotlinx.coroutines.launch

@Composable
fun TeamLeaderConsole(navController: NavController) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
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
        Text("Team Leader Console", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Review Queue", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(tasks) { task ->
                ReviewCard(task)
            }
        }
    }
}

@Composable
fun ReviewCard(task: Task) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(task.artworkTitle, style = MaterialTheme.typography.titleSmall)
            Text("Seg: ${task.segmentIndex}")
            Text("Status: ${task.status}")
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Button(onClick = { /* Approve Logic */ }, modifier = Modifier.weight(1f)) {
                    Text("Approve", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { /* Reject Logic */ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Reject", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
