package com.hse.coursework.nutrik.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.sql.Timestamp


object RetrofitInstance {
    private const val BASE_URL = "https://apiserver-de52.onrender.com/"

    val api: OpenRouterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApiService::class.java)
    }
}


data class GPTResponse(
    var message: String,
    var error: String? = null,
    var timestamp: Timestamp = Timestamp(System.currentTimeMillis())
)


data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

interface OpenRouterApiService {
    @Headers("Content-Type: application/json")
    @POST("chat")
    suspend fun getChatCompletion(@Body request: OpenRouterRequest): OpenRouterResponse
}
