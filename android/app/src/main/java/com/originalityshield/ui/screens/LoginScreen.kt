package com.originalityshield.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.originalityshield.network.RetrofitClient
import com.originalityshield.network.UserLogin
import com.originalityshield.network.GeoValidateRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
                    // Call login with UserLogin object (JSON body)
                    val response = RetrofitClient.api.login(UserLogin(username, password))
                    RetrofitClient.authToken = response.accessToken
                    Log.d("Login", "Login successful, token saved.")

                    // Geo Validation (Mock Location)
                    statusMessage = "Validating Location..."
                    val geo = RetrofitClient.api.validateGeo(
                        GeoValidateRequest(
                            latitude = 26.05,
                            longitude = 86.05,
                            villageId = 1
                        )
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
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("LoginError", "HTTP ${e.code()}: $errorBody")
                    statusMessage = "Error ${e.code()}: See logs for details"
                } catch (e: Exception) {
                    Log.e("LoginError", "Unexpected error: ${e.message}")
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
