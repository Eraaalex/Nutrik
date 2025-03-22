package com.hse.coursework.nutrik.repository.chat

import android.util.Log
import com.hse.coursework.nutrik.network.GPTResponse
import com.hse.coursework.nutrik.network.Message
import com.hse.coursework.nutrik.network.OpenRouterRequest
import com.hse.coursework.nutrik.network.OpenRouterResponse
import com.hse.coursework.nutrik.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import javax.inject.Inject


class ChatRepository @Inject constructor() {

    private var response: GPTResponse = GPTResponse(message = "", error = null);
    private final val MODEL = "deepseek/deepseek-chat-v3-0324:free"


    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        val request = OpenRouterRequest(
            model = MODEL,
            messages = listOf(
                Message(role = "user", content = userMessage)
            )
        )

        try {
            val responseOpenRouter = RetrofitInstance.api.getChatCompletion(request)
            updateResponse(responseOpenRouter, null)
            Log.e(
                "ChatRepository",
                "Response: ${responseOpenRouter.choices.firstOrNull()?.message?.content}"
            )
            return@withContext responseOpenRouter.choices.firstOrNull()?.message?.content
                ?: "Ошибка: пустой ответ"
        } catch (e: Exception) {
            e.printStackTrace()
            updateResponse(OpenRouterResponse(emptyList()), e.localizedMessage)
            Log.e("ChatRepository", "Response: ${e.localizedMessage} ${e.stackTraceToString()}")
            return@withContext "Советов пока нет"
        }
    }

    private fun updateResponse(answer: OpenRouterResponse, error: String? = null) {
        response.message =
            (answer.choices.firstOrNull()?.message?.content ?: "Ошибка: пустой ответ")
        response.error = error
        response.timestamp = Timestamp(System.currentTimeMillis())
    }
}
