package com.originalityshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.originalityshield.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("artist1") }
    var password by remember { mutableStateOf("password") }
    var statusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Village Gateway", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                try {
                    statusMessage = "Authenticating..."
                    val response = RetrofitClient.api.login(username, password)
                    RetrofitClient.authToken = response.accessToken

                    // Geo Validation (Mock Location)
                    statusMessage = "Validating Location..."
                    val geo = RetrofitClient.api.validateGeo(
                        com.originalityshield.network.GeoValidateRequest(26.05, 86.05, 1) // Center of mock polygon
                    )

                    if (geo.valid) {
                        if (response.role == "team_leader") {
                            navController.navigate("leader")
                        } else {
                            navController.navigate("dashboard/${response.userId}")
                        }
                    } else {
                        statusMessage = "Location Check Failed: ${geo.message}"
                    }
                } catch (e: Exception) {
                    statusMessage = "Error: ${e.message}"
                }
            }
        }) {
            Text("Enter Village Registry")
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(statusMessage)
        }
    }
}
