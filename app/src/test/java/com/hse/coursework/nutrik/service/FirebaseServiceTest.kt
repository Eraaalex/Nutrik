package com.hse.coursework.nutrik.service

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.hse.coursework.nutrik.model.ConsumptionDTO
import com.hse.coursework.nutrik.model.ProductDTO
import com.hse.coursework.nutrik.model.ProgressItem
import com.hse.coursework.nutrik.model.ProgressRemoteEntity
import com.hse.coursework.nutrik.model.toDomain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseServiceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var service: FirebaseService
    private val testDispatcher = StandardTestDispatcher()

    private val userId = "u1"
    private val today = LocalDate.of(2024, 12, 31)
    private val dateKey = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    @Before
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        firestore = mockk(relaxed = true)
        service = FirebaseService(firestore)

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun teardown() {
        unmockkAll()
        kotlinx.coroutines.Dispatchers.resetMain()
    }


    @Test
    fun `getProductById returns null if DTO is null`() = runTest {
        val id = "missing"
        val docRef = mockk<DocumentReference>()
        val document = mockk<DocumentSnapshot>()

        every { firestore.collection("products") } returns mockk {
            every { document(id) } returns docRef
        }
        every { docRef.get() } returns Tasks.forResult(document)
        every { document.toObject(ProductDTO::class.java) } returns null

        val result = service.getProductById(id)
        assertNull(result)
    }


    @Test
    fun `getProgressForDate returns null when doc missing`() = runTest {
        val usersCol = mockk<CollectionReference>()
        val progressCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns docRef
        every { docRef.collection("progress") } returns progressCol
        every { progressCol.document(dateKey) } returns docRef

        val snap = mockk<DocumentSnapshot>()
        every { docRef.get() } returns Tasks.forResult(snap)
        every { snap.exists() } returns false

        assertNull(service.getProgressForDate(userId, today))
    }

    @Test
    fun `getProgressForDate returns ProgressItem when doc exists`() = runTest {
        val usersCol = mockk<CollectionReference>()
        val progressCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns docRef
        every { docRef.collection("progress") } returns progressCol
        every { progressCol.document(dateKey) } returns docRef

        val remote = ProgressRemoteEntity(
            date = dateKey,
            protein = 10, fat = 5, carbs = 20,
            calories = 100, sugar = 2, salt = 1,
            violationsCount = 0
        )
        val snap = mockk<DocumentSnapshot>()
        every { docRef.get() } returns Tasks.forResult(snap)
        every { snap.exists() } returns true
        every { snap.toObject(ProgressRemoteEntity::class.java) } returns remote

        val got = service.getProgressForDate(userId, today)
        assertEquals(ProgressItem.fromRemoteEntity(remote), got)
    }


    @Test
    fun `getProductData returns mapped list and lastDocument`() = runTest {

        val base = mockk<CollectionReference>()
        val ordered = mockk<Query>()
        val limited = mockk<Query>()
        val snap = mockk<QuerySnapshot>()

        val doc1 = mockk<DocumentSnapshot>()
        val dto1 = ProductDTO(id = "p1", code = "c", name = "A")
        every { doc1.id } returns "p1"
        every { doc1.toObject(ProductDTO::class.java) } returns dto1
        val doc2 = mockk<DocumentSnapshot>()
        val dto2 = ProductDTO(id = "p2", code = "c", name = "B")
        every { doc2.id } returns "p2"
        every { doc2.toObject(ProductDTO::class.java) } returns dto2

        every { firestore.collection("products") } returns base
        every { base.orderBy("name") } returns ordered
        every { ordered.limit(2) } returns limited
        every { limited.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc1, doc2)

        val result = service.getProductData(2, null, null)
        assertEquals(2, result.data.size)
        assertEquals("p1", result.data[0].id)
        assertEquals(doc2, result.lastDocument)
    }


    @Test
    fun `getProductById returns domain when DTO present`() = runTest {
        val docRef = mockk<DocumentReference>()
        val snap = mockk<DocumentSnapshot>()
        val dto = ProductDTO(id="x", code="cd")
        every { firestore.collection("products") } returns mockk {
            every { document("x") } returns docRef
        }
        every { docRef.get() } returns Tasks.forResult(snap)
        every { snap.toObject(ProductDTO::class.java) } returns dto
        every { snap.id } returns "x"

        val out = service.getProductById("x")
        assertEquals(dto.toDomain("x"), out)
    }

    @Test
    fun `getProductById returns null when DTO missing`() = runTest {
        val docRef = mockk<DocumentReference>()
        val snap = mockk<DocumentSnapshot>()
        every { firestore.collection("products") } returns mockk {
            every { document("x") } returns docRef
        }
        every { docRef.get() } returns Tasks.forResult(snap)
        every { snap.toObject(ProductDTO::class.java) } returns null

        assertNull(service.getProductById("x"))
    }


    @Test
    fun `updateUserFavorites calls set with merge`() = runTest {
        val docRef = mockk<DocumentReference>()
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns docRef
        }
        every { docRef.set(mapOf("favoriteProductIds" to listOf("a","b")), SetOptions.merge()) }
            .returns(Tasks.forResult(null))

        service.updateUserFavorites(userId, listOf("a","b"))
    }

    @Test
    fun `getUserFavorites returns list or empty`() = runTest {
        val docRef = mockk<DocumentReference>()
        val snap = mockk<DocumentSnapshot>()
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns docRef
        }
        every { docRef.get() } returns Tasks.forResult(snap)
        every { snap.get("favoriteProductIds") as? List<String> } returns listOf("x","y")

        assertEquals(listOf("x","y"), service.getUserFavorites(userId))
    }

    @Test
    fun `getConsumptionByDate returns matching DTO or null`() = runTest {
        val day = "2024-05-01"; val prod = "p1"
        val docRef = mockk<DocumentReference>()
        val snap = mockk<DocumentSnapshot>()
        val list = ConsumptionList(
            consumptions = mutableListOf(
                ConsumptionDTO(prod, "pn", userId, day, 10.0)
            )
        )
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns docRef
        }
        every { docRef.collection("consumption") } returns mockk {
            every { document(day) } returns docRef
        }
        every { docRef.get() } returns Tasks.forResult(snap)
        every { snap.exists() } returns true
        every { snap.toObject(ConsumptionList::class.java) } returns list

        val got = service.getConsumptionByDate(userId, day, prod)
        assertEquals(list.consumptions.first(), got)
    }


    @Test
    fun `getConsumptionsByPeriod filters by id range`() = runTest {
        val from="2024-01-01"; val to="2024-12-31"
        val col = mockk<CollectionReference>()
        val snap = mockk<QuerySnapshot>()
        val doc1 = mockk<QueryDocumentSnapshot>()

        val dto1 = ConsumptionDTO("p1","pn",userId,from,5.0)
        every { doc1.id } returns from
        every { doc1.toObject(ConsumptionList::class.java) } returns ConsumptionList(mutableListOf(dto1))
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                every { collection("consumption") } returns col
            }
        }
        every { col.whereGreaterThanOrEqualTo(FieldPath.documentId(), from) } returns col
        every { col.whereLessThanOrEqualTo(FieldPath.documentId(), to) } returns col
        every { col.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc1)
        every { snap.iterator() } returns mutableListOf(doc1).iterator()


        val list = service.getConsumptionsByPeriod(userId, from, to)
        assertEquals(1, list.size)
        assertEquals(dto1, list[0])
    }


    @Test
    fun `getProductData with startAfter returns next page`() = runTest {
        val base = mockk<CollectionReference>()
        val ordered = mockk<Query>()
        val limited = mockk<Query>()
        val after = mockk<Query>()
        val snap = mockk<QuerySnapshot>()
        val startAfterDoc = mockk<DocumentSnapshot>()
        val doc = mockk<DocumentSnapshot>()
        val dto = ProductDTO(id = "p3", code = "code3", name = "C")

        every { firestore.collection("products") } returns base
        every { base.orderBy("name") } returns ordered
        every { ordered.limit(1) } returns limited
        every { limited.startAfter(startAfterDoc) } returns after
        every { after.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc)
        every { doc.toObject(ProductDTO::class.java) } returns dto
        every { doc.id } returns "p3"

        val result = service.getProductData(1, startAfterDoc, null)
        assertEquals(1, result.data.size)
        assertEquals(dto.toDomain("p3"), result.data[0])
        assertEquals(doc, result.lastDocument)
    }

    @Test
    fun `getProductData with query filters by name`() = runTest {
        val base = mockk<CollectionReference>()
        val ordered = mockk<Query>()
        val limited = mockk<Query>()
        val filtered1 = mockk<Query>()
        val filtered2 = mockk<Query>()
        val snap = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()
        val queryStr = "Apple"
        val dto = ProductDTO(id = "p4", code = "code4", name = "Apple Pie")

        every { firestore.collection("products") } returns base
        every { base.orderBy("name") } returns ordered
        every { ordered.limit(2) } returns limited
        every { limited.whereGreaterThanOrEqualTo("name", queryStr) } returns filtered1
        every { filtered1.whereLessThanOrEqualTo("name", "${queryStr}\uf8ff") } returns filtered2
        every { filtered2.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc)
        every { doc.toObject(ProductDTO::class.java) } returns dto
        every { doc.id } returns "p4"

        val result = service.getProductData(2, null, queryStr)
        assertEquals(1, result.data.size)
        assertEquals(dto.toDomain("p4"), result.data[0])
    }

    @Test
    fun `getProgressForPeriod returns list of progress items within range`() = runTest {
        val usersCol = mockk<CollectionReference>()
        val progressCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()
        val snap = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val fromDate = LocalDate.of(2024, 1, 1)
        val toDate = LocalDate.of(2024, 1, 2)
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val key1 = fromDate.format(fmt)
        val key2 = toDate.format(fmt)
        val remote1 = ProgressRemoteEntity(key1, 1,1,1,100,1,1,0)
        val remote2 = ProgressRemoteEntity(key2, 2,2,2,200,2,2,1)

        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns docRef
        every { docRef.collection("progress") } returns progressCol
        every { progressCol.whereGreaterThanOrEqualTo(FieldPath.documentId(), key1) } returns query1
        every { query1.whereLessThanOrEqualTo(FieldPath.documentId(), key2) } returns query2
        every { query2.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc1, doc2)
        every { doc1.id } returns key1
        every { doc1.toObject(ProgressRemoteEntity::class.java) } returns remote1
        every { doc2.id } returns key2
        every { doc2.toObject(ProgressRemoteEntity::class.java) } returns remote2

        val result = service.getProgressForPeriod(userId, fromDate, toDate)
        assertEquals(2, result.size)
        assertEquals(fromDate to ProgressItem.fromRemoteEntity(remote1), result[0])
        assertEquals(toDate to ProgressItem.fromRemoteEntity(remote2), result[1])
    }

    @Test
    fun `getConsumptionByDates returns all consumptions in date range`() = runTest {
        val usersCol = mockk<CollectionReference>()
        val consumCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()
        val snap = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val day1 = LocalDate.of(2024, 5, 1)
        val day2 = LocalDate.of(2024, 5, 3)
        val day3 = LocalDate.of(2024, 5, 2)
        val dates = listOf(day1, day2, day3)
        val keyMin = day1.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val keyMax = day2.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val cons1 = ConsumptionDTO("p1", "pn1", userId, keyMin, 5.0)
        val cons2 = ConsumptionDTO("p2", "pn2", userId, keyMin, 10.0)
        val cons3 = ConsumptionDTO("p3", "pn3", userId, day3.format(DateTimeFormatter.ISO_LOCAL_DATE), 15.0)

        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns docRef
        every { docRef.collection("consumption") } returns consumCol
        every { consumCol.whereGreaterThanOrEqualTo(FieldPath.documentId(), keyMin) } returns query1
        every { query1.whereLessThanOrEqualTo(FieldPath.documentId(), keyMax) } returns query2
        every { query2.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc1, doc2)

        every { doc1.id } returns keyMin
        every { doc2.id } returns day3.format(DateTimeFormatter.ISO_LOCAL_DATE)
        every { doc1.toObject(ConsumptionList::class.java) } returns ConsumptionList(mutableListOf(cons1, cons2))
        every { doc2.toObject(ConsumptionList::class.java) } returns ConsumptionList(mutableListOf(cons3))

        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns docRef
        every { docRef.collection("consumption") } returns consumCol
        every { consumCol.whereGreaterThanOrEqualTo(FieldPath.documentId(), keyMin) } returns query1
        every { query1.whereLessThanOrEqualTo(FieldPath.documentId(), keyMax) } returns query2
        every { query2.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc1, doc2)
        every { doc1.toObject(ConsumptionList::class.java) } returns ConsumptionList(mutableListOf(cons1, cons2))
        every { doc2.toObject(ConsumptionList::class.java) } returns ConsumptionList(mutableListOf(cons3))

        val result = service.getConsumptionByDates(userId, dates)
        assertEquals(3, result.size)
        assertTrue(result.containsAll(listOf(cons1, cons2, cons3)))
    }

    @Test
    fun `searchProducts returns products matching normalized query`() = runTest {
        val base = mockk<CollectionReference>()
        val ordered = mockk<Query>()
        val start = mockk<Query>()
        val end = mockk<Query>()
        val snap = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()
        val normalized = "test"
        val dto = ProductDTO(id = "p1", code = "c", name = "Name")

        every { firestore.collection("products") } returns base
        every { base.orderBy("name_lower") } returns ordered
        every { ordered.startAt(normalized) } returns start
        every { start.endAt("$normalized\uf8ff") } returns end
        every { end.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc)
        every { doc.id } returns "p1"
        every { doc.toObject(ProductDTO::class.java) } returns dto

        val result = service.searchProducts("  Test  ")
        assertEquals(1, result.size)
        assertEquals(dto.toDomain("p1"), result[0])
    }

    @Test
    fun `getProductByName returns domain when product found`() = runTest {
        val base = mockk<CollectionReference>()
        val queryRef = mockk<Query>()
        val snap = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()
        val dto = ProductDTO(id = "p5", code = "c5", name = "Banana")

        every { firestore.collection("products") } returns base
        every { base.whereEqualTo("name", "Banana") } returns queryRef
        every { queryRef.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns listOf(doc)
        every { doc.id } returns "p5"
        every { doc.toObject(ProductDTO::class.java) } returns dto

        val result = service.getProductByName("Banana")
        assertEquals(dto.toDomain("p5"), result)
    }

    @Test
    fun `getProductByName returns null when no product found`() = runTest {
        val base = mockk<CollectionReference>()
        val queryRef = mockk<Query>()
        val snap = mockk<QuerySnapshot>()

        every { firestore.collection("products") } returns base
        every { base.whereEqualTo("name", "Unknown") } returns queryRef
        every { queryRef.get() } returns Tasks.forResult(snap)
        every { snap.documents } returns emptyList()

        val result = service.getProductByName("Unknown")
        assertNull(result)
    }

    @Test
    fun `saveProgressForDate calls set with remote entity`() = runTest {
        val usersCol = mockk<CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val progressCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val date = LocalDate.of(2025, 5, 23)
        val key = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val item = ProgressItem(date, 3,4,5,150,6,7,2)

        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns userDoc
        every { userDoc.collection("progress") } returns progressCol
        every { progressCol.document(key) } returns docRef
        every { docRef.set(item.toRemoteEntity()) } returns Tasks.forResult(null)

        service.saveProgressForDate(userId, item)
        verify { docRef.set(item.toRemoteEntity()) }
    }

    @Test
    fun `insertConsumption adds new consumption when none exists`() = runTest {
        val day = "2024-06-01"
        val consumption = ConsumptionDTO("p1", "pn", userId, day, 10.0)
        val usersCol = mockk<CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val consumCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val document = mockk<DocumentSnapshot>()
        val task = mockk<com.google.android.gms.tasks.Task<DocumentSnapshot>>()

        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns userDoc
        every { userDoc.collection("consumption") } returns consumCol
        every { consumCol.document(day) } returns docRef
        every { docRef.get() } returns task
        every { task.addOnSuccessListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
            every { document.exists() } returns false
            listener.onSuccess(document)
            task
        }
        every { docRef.set(match<Map<String, Any>> { map ->
            (map["consumptions"] as? List<*>)?.contains(consumption) == true
        }) } returns Tasks.forResult(null)

        service.insertConsumption(consumption)
        verify { docRef.set(any()) }
    }

    @Test
    fun `insertConsumption updates existing consumption when present`() = runTest {
        val day = "2024-06-02"
        val old = ConsumptionDTO("p1", "pn", userId, day, 5.0)
        val updated = ConsumptionDTO("p1", "pn", userId, day, 15.0)
        val usersCol = mockk<CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val consumCol = mockk<CollectionReference>()
        val docRef = mockk<DocumentReference>()
        val document = mockk<DocumentSnapshot>()
        val task = mockk<com.google.android.gms.tasks.Task<DocumentSnapshot>>()

        every { firestore.collection("users") } returns usersCol
        every { usersCol.document(userId) } returns userDoc
        every { userDoc.collection("consumption") } returns consumCol
        every { consumCol.document(day) } returns docRef
        every { docRef.get() } returns task
        every { task.addOnSuccessListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
            every { document.exists() } returns true
            every { document.toObject(ConsumptionList::class.java) } returns ConsumptionList(mutableListOf(old))
            listener.onSuccess(document)
            task
        }
        every { docRef.set(match<Map<String, Any>> { map ->
            (map["consumptions"] as? List<*>)?.firstOrNull() == updated
        }) } returns Tasks.forResult(null)

        service.insertConsumption(updated)
        verify { docRef.set(any()) }
    }

}
