package com.hse.coursework.nutrik.service

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hse.coursework.nutrik.model.ConsumptionDTO
import com.hse.coursework.nutrik.model.ProductDTO
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.ProgressRemoteEntity
import com.hse.coursework.nutrik.model.dto.toDomain
import com.hse.coursework.nutrik.model.toDomain
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class PaginatedResult<T>(
    val data: List<T>,
    val lastDocument: DocumentSnapshot?
)

class FirebaseService @Inject constructor(private val firebaseFirestore: FirebaseFirestore) {
    private companion object {
        const val PRODUCT_COLLECTION = "products"
        const val PROGRESS_COLLECTION = "progress"
        const val CONSUMPTION_COLLECTION = "consumption"
        const val DATE_FIELD = "yyyy-MM-dd"
    }


    /** users/{userId}/progress/{date} */
    suspend fun getProgressForDate(userId: String, date: LocalDate): ProgressItem? {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
            .document(userId)
            .collection(PROGRESS_COLLECTION)
            .document(date.format(DateTimeFormatter.ofPattern(DATE_FIELD)))

        return try {
            val document = userRef.get().await()
            if (document.exists()) {
                val remoteEntity = document.toObject(ProgressRemoteEntity::class.java)
                remoteEntity?.let { ProgressItem.fromRemoteEntity(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(
                "ProgressRepository",
                "Failed to load progress for date $date: ${e.localizedMessage}"
            )
            null
        }
    }


    suspend fun getProductData(
        limit: Int,
        startAfter: DocumentSnapshot? = null,
        query: String? = null
    ): PaginatedResult<ProductEntity> {
        var firestoreQuery = firebaseFirestore.collection(PRODUCT_COLLECTION)
            .orderBy("name")
            .limit(limit.toLong())

        if (startAfter != null) {
            firestoreQuery = firestoreQuery.startAfter(startAfter)
        }

        if (!query.isNullOrBlank()) {
            firestoreQuery = firestoreQuery
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
        }

        val result = firestoreQuery.get().await()

        val products = result.documents.mapNotNull { doc ->
            doc.toObject(ProductDTO::class.java)?.toDomain(doc.id)
        }
        val lastDoc = if (result.documents.isNotEmpty()) result.documents.last() else null

        return PaginatedResult(products, lastDoc)
    }

    suspend fun getProductById(id: String): ProductEntity? {
        val doc = firebaseFirestore.collection(PRODUCT_COLLECTION)
            .document(id)
            .get()
            .await()
        return doc.toObject(ProductDTO::class.java)?.toDomain(doc.id)
    }

    suspend fun updateUserFavorites(userId: String, favoriteIds: List<String>) {
        firebaseFirestore.collection("users")
            .document(userId)
            .set(mapOf("favoriteProductIds" to favoriteIds), SetOptions.merge())
            .await()
    }

    suspend fun getUserFavorites(userId: String): List<String> {
        val snapshot = firebaseFirestore.collection("users")
            .document(userId)
            .get()
            .await()

        @Suppress("UNCHECKED_CAST")
        return snapshot.get("favoriteProductIds") as? List<String> ?: emptyList()
    }

    suspend fun insertConsumption(consumption: ConsumptionDTO) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
            .document(consumption.userId)
            .collection("consumption")
            .document(consumption.date)

        userRef.get().addOnSuccessListener { document ->
            val consumptionList = if (document.exists()) {
                document.toObject(ConsumptionList::class.java)?.consumptions ?: mutableListOf()
            } else {
                mutableListOf()
            }

            val existingIndex =
                consumptionList.indexOfFirst { it.productId == consumption.productId }
            if (existingIndex != -1) {
                consumptionList[existingIndex] =
                    consumptionList[existingIndex].copy(weight = consumption.weight)
            } else {
                consumptionList.add(consumption)
            }

            userRef.set(mapOf("consumptions" to consumptionList))
        }
    }

    suspend fun getConsumptionByDate(
        userId: String,
        date: String,
        productId: String
    ): ConsumptionDTO? {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
            .document(userId)
            .collection("consumption")
            .document(date)

        val document = userRef.get().await()
        if (document.exists()) {
            val consumptionList =
                document.toObject(ConsumptionList::class.java)?.consumptions ?: return null
            return consumptionList.firstOrNull { it.productId == productId }
        }
        return null
    }

    suspend fun getConsumptionsByPeriod(
        userId: String,
        startDate: String,
        endDate: String
    ): List<ConsumptionDTO> {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users")
            .document(userId)
            .collection("consumption")

        val documents = userRef.get().await()
        val result = mutableListOf<ConsumptionDTO>()

        for (document in documents) {
            val date = document.id
            if (date >= startDate && date <= endDate) {
                val consumptionList =
                    document.toObject(ConsumptionList::class.java)?.consumptions ?: continue
                result.addAll(consumptionList)
            }
        }
        return result
    }

    suspend fun getProductByName(name: String): ProductEntity? {
        val result = firebaseFirestore.collection(PRODUCT_COLLECTION)
            .whereEqualTo("name", name)
            .get()
            .await()

        return result.documents.firstOrNull()?.let { doc ->
            doc.toObject(ProductDTO::class.java)?.toDomain(doc.id)
        }
    }

    suspend fun searchProducts(query: String): List<ProductEntity> {
        val normalizedQuery = query.lowercase().trim()

        val result = firebaseFirestore.collection(PRODUCT_COLLECTION)
            .orderBy("name_lower")
            .startAt(normalizedQuery)
            .endAt("$normalizedQuery\uf8ff")
            .get()
            .await()

        return result.documents.mapNotNull { doc ->
            doc.toObject(ProductDTO::class.java)?.toDomain(doc.id)
        }
    }

    suspend fun saveProgressForDate(userId: String, progressItem: ProgressItem) {
        val dateKey = progressItem.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        firebaseFirestore
            .collection("users")
            .document(userId)
            .collection(PROGRESS_COLLECTION)
            .document(dateKey)
            .set(progressItem.toRemoteEntity())
            .await()
    }

    suspend fun getProgressForPeriod(
        userId: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<Pair<LocalDate, ProgressItem>> {
        val db = firebaseFirestore
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val collectionRef = db.collection("users")
            .document(userId)
            .collection("progress")

        val from = fromDate.format(formatter)
        val to = toDate.format(formatter)

        return try {
            val querySnapshot = collectionRef
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), from)
                .whereLessThanOrEqualTo(FieldPath.documentId(), to)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                val date = LocalDate.parse(doc.id, formatter)
                val remoteEntity = doc.toObject(ProgressRemoteEntity::class.java)
                remoteEntity?.let {
                    date to ProgressItem.fromRemoteEntity(it)
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "getProgressForWeek failed: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getConsumptionByDates(
        userId: String,
        dates: List<LocalDate>
    ): List<ConsumptionDTO> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val from = dates.minOf { it }.format(formatter)
        val to = dates.maxOf { it }.format(formatter)

        val snapshot = firebaseFirestore
            .collection("users")
            .document(userId)
            .collection(CONSUMPTION_COLLECTION)
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), from)
            .whereLessThanOrEqualTo(FieldPath.documentId(), to)
            .get()
            .await()

        Log.e("RemoteDataSource", "snapshot = ${snapshot.documents.map { it.id }}")

        return snapshot.documents.flatMap { doc ->
            doc.toObject(ConsumptionList::class.java)?.consumptions.orEmpty()
        }
    }


}


data class ConsumptionList(
    val consumptions: MutableList<ConsumptionDTO> = mutableListOf()
)
