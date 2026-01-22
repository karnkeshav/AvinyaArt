package com.example.ddcp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// DTOs
data class UserLogin(val username: String, val password: String)
data class Token(val access_token: String, val token_type: String, val user_id: Int, val role: String)
data class GeoValidateRequest(val latitude: Double, val longitude: Double, val village_id: Int)
data class GeoResponse(val valid: Boolean, val message: String)
data class Task(
    val task_id: Int,
    val artwork_title: String,
    val segment_index: Int,
    val master_image_url: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val status: String
)
data class TelemetryData(
    val user_id: Int,
    val pressure_samples: List<Float>,
    val velocity_samples: List<Float>,
    val jitter_samples: List<Float>,
    val stroke_timestamps: List<Long>
)
data class TaskSubmission(
    val submission_url: String,
    val telemetry_data: TelemetryData
)
data class SubmissionResponse(val status: String, val human_confidence: Float)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body login: UserLogin): Token

    @POST("auth/geo/validate")
    suspend fun validateGeo(@Body request: GeoValidateRequest): GeoResponse

    @GET("tasks/assignments")
    suspend fun getAssignments(@retrofit2.http.Query("user_id") userId: Int): List<Task>

    @POST("tasks/submit/{task_id}")
    suspend fun submitTask(@Path("task_id") taskId: Int, @Body submission: TaskSubmission): SubmissionResponse
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/" // Android Emulator loopback to host

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
