package com.originalityshield.network

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// DTOs
data class UserLogin(val username: String, val password: String)

data class Token(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user_id") val userId: Int? = null,
    val role: String? = null
)

data class GeoValidateRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("village_id") val villageId: Int
)

data class GeoResponse(val valid: Boolean, val message: String)

data class Task(
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("artwork_title") val artworkTitle: String,
    @SerializedName("segment_index") val segmentIndex: Int,
    @SerializedName("master_image_url") val masterImageUrl: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val status: String
)

data class TelemetryData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("pressure_samples") val pressureSamples: List<Float>,
    @SerializedName("velocity_samples") val velocitySamples: List<Float>,
    @SerializedName("jitter_samples") val jitterSamples: List<Float>,
    @SerializedName("stroke_timestamps") val strokeTimestamps: List<Long>
)

data class TaskSubmission(
    @SerializedName("submission_url") val submissionUrl: String,
    @SerializedName("telemetry_data") val telemetryData: TelemetryData
)

data class SubmissionResponse(
    val status: String,
    @SerializedName("human_confidence") val humanConfidence: Float
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body login: UserLogin): Token

    @POST("auth/geo/validate")
    suspend fun validateGeo(@Body request: GeoValidateRequest): GeoResponse

    @GET("tasks/assignments")
    suspend fun getAssignments(): List<Task> // Removed userId parameter

    @POST("tasks/submit/{task_id}")
    suspend fun submitTask(@Path("task_id") taskId: Int, @Body submission: TaskSubmission): SubmissionResponse
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"
    var authToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Connection", "close")
        authToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
