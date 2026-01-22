package com.example.ddcp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ddcp.ui.theme.DDCPTheme
import com.example.ddcp.ui.screens.LoginScreen
import com.example.ddcp.ui.screens.ArtistDashboard
import com.example.ddcp.ui.screens.CreativeCanvas
import com.example.ddcp.ui.screens.TeamLeaderConsole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DDCPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("dashboard/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            ArtistDashboard(navController, userId)
        }
        composable("canvas/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
            CreativeCanvas(navController, taskId)
        }
        composable("leader") {
            TeamLeaderConsole(navController)
        }
    }
}
