package com.hse.coursework.nutrik.repository.chat

import android.util.Log
import com.hse.coursework.nutrik.data.dao.AdviceHistoryEntity
import com.hse.coursework.nutrik.data.dao.ChatDao
import com.hse.coursework.nutrik.data.dao.ChatStatusEntity
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.network.GPTResponse
import com.hse.coursework.nutrik.network.Message
import com.hse.coursework.nutrik.network.OpenRouterRequest
import com.hse.coursework.nutrik.network.OpenRouterResponse
import com.hse.coursework.nutrik.network.RetrofitInstance
import com.hse.coursework.nutrik.repository.progress.WeekProgressResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import javax.inject.Inject


//class ChatRepository @Inject constructor() {
//
//    private var response: GPTResponse = GPTResponse(message = "", error = null);
//    private final val MODEL = "deepseek/deepseek-chat-v3-0324:free"
//
//
//    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
//        val request = OpenRouterRequest(
//            model = MODEL,
//            messages = listOf(
//                Message(role = "user", content = userMessage)
//            )
//        )
//
//        try {
//            val responseOpenRouter = RetrofitInstance.api.getChatCompletion(request)
//            updateResponse(responseOpenRouter, null)
//            Log.e(
//                "ChatRepository",
//                "Response: ${responseOpenRouter.choices.firstOrNull()?.message?.content}"
//            )
//            return@withContext responseOpenRouter.choices.firstOrNull()?.message?.content
//                ?: "Ошибка: пустой ответ"
//        } catch (e: Exception) {
//            e.printStackTrace()
//            updateResponse(OpenRouterResponse(emptyList()), e.localizedMessage)
//            Log.e("ChatRepository", "Response: ${e.localizedMessage} ${e.stackTraceToString()}")
//            return@withContext "Советов пока нет"
//        }
//    }
//
//    private fun updateResponse(answer: OpenRouterResponse, error: String? = null) {
//        response.message =
//            (answer.choices.firstOrNull()?.message?.content ?: "Ошибка: пустой ответ")
//        response.error = error
//        response.timestamp = Timestamp(System.currentTimeMillis())
//    }
//}

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
) {
    private val MODEL = "deepseek/deepseek-chat-v3-0324:free"

    /** Поток текущего "живого" совета */
    fun adviceFlowFor(userId: String): Flow<String> =
        chatDao.getChatStatus(userId)
            .map { it?.lastAdviceText ?: "Советов пока нет" }

    /**
     * Возвращает либо сохранённый последний совет, либо запросит новый,
     * если:
     *   — новых записей > lastConsumptionCount + 3,
     *   — или прошло >12ч
     */
    suspend fun fetchAdviceIfNeeded(
        weekData : List<WeekProgressResult>,
        userPrefs: User?,
        userId: String,
        todayConsumptionCount: Int
    ): String = withContext(Dispatchers.IO) {
        val status = chatDao.getChatStatus(userId).firstOrNull()
        val now = System.currentTimeMillis()
        Log.e("ChatRepository", "Status: $status, todayCount: $todayConsumptionCount, now: $now")

        val needsNew =
            status == null ||
                    todayConsumptionCount - (status.lastConsumptionCount) >= 3 ||
                    now - (status.lastAdviceTimestamp) >= 12 * 60 * 60 * 1000
        Log.e("ChatRepo", "Now $now, Status $status, LastTimestamp ${status?.lastAdviceTimestamp}")

        if (!needsNew) {
             return@withContext status!!.lastAdviceText
        }


        val message   =  "Мой рацион за неделю: $weekData. Учитывай: $userPrefs. Дай краткие советы по питанию не более 2-3 советов, без нумерации и выделителей, не более 70 слов и мотивирующе."

        val tip = try {
            val req = OpenRouterRequest(model = MODEL, messages = listOf(
                Message(role="user", content = message)
            ))
            RetrofitInstance.api.getChatCompletion(req).choices.first().message.content
        } catch(e: Exception) {
            Log.e("ChatRepository", "Ошибка получения совета: ${e.localizedMessage}", e)
            "Советов пока нет"
        }

        // Сохраняем и в историю, и в статус
        if (tip.length < 20) {
            Log.e("ChatRepository", "Получен пустой совет, не сохраняем")
            return@withContext "Советов пока нет"
        }
        chatDao.insertAdviceHistory(
            AdviceHistoryEntity(
                userId      = userId,
                adviceText  = tip,
                timestamp   = now
            )
        )
        chatDao.upsertChatStatus(
            ChatStatusEntity(
                userId               = userId,
                lastAdviceText       = tip,
                lastAdviceTimestamp  = now,
                lastConsumptionCount = todayConsumptionCount
            )
        )
        return@withContext tip
    }
}

