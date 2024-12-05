package com.hse.coursework.nutrik.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hse.coursework.nutrik.model.ConsumptionDTO
import com.hse.coursework.nutrik.model.Product
import com.hse.coursework.nutrik.model.ProductDTO
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.toDomain
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PaginatedResult<T>(
    val data: List<T>,
    val lastDocument: DocumentSnapshot?
)

class FirebaseService @Inject constructor(private val firebaseFirestore: FirebaseFirestore) {
    private companion object {
        const val PRODUCT_COLLECTION = "products"
        const val PROGRESS_COLLECTION = "progress"
    }

    suspend fun getProgressData(): List<ProgressItem> {
        val result = firebaseFirestore.collection(PROGRESS_COLLECTION)
            .get()
            .await()
        return result.documents.mapNotNull { doc ->
            doc.toObject(ProgressItem::class.java)
        }
    }

    suspend fun getProductData(
        limit: Int,
        startAfter: DocumentSnapshot? = null,
        query: String? = null
    ): PaginatedResult<Product> {
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

    suspend fun getProductById(id: String): Product {
        val doc = firebaseFirestore.collection(PRODUCT_COLLECTION)
            .document(id)
            .get()
            .await()
        return doc.toObject(ProductDTO::class.java)?.toDomain(doc.id)
            ?: throw IllegalStateException("Product not found")
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

            val existingIndex = consumptionList.indexOfFirst { it.productId == consumption.productId }
            if (existingIndex != -1) {
                consumptionList[existingIndex] = consumptionList[existingIndex].copy(weight = consumption.weight)
            } else {
                consumptionList.add(consumption)
            }

            userRef.set(mapOf("consumptions" to consumptionList))
        }
    }

}


data class ConsumptionList(
    val consumptions: MutableList<ConsumptionDTO> = mutableListOf()
)