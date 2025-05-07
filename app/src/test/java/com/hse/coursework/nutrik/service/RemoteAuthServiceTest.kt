package com.hse.coursework.nutrik.service

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.hse.coursework.nutrik.model.dto.FirestoreUserDTO
import com.hse.coursework.nutrik.model.dto.User
import com.hse.coursework.nutrik.model.dto.toDomain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteAuthServiceTest {

    private val firestore: FirebaseFirestore = mockk()
    private val auth: FirebaseAuth = mockk()
    private val user: FirebaseUser = mockk()
    private lateinit var service: RemoteAuthService

    private val userId = "test_uid"
    private val email = "test@example.com"

    @Before
    fun setup() {
        service = RemoteAuthService(firestore, auth)
        every { auth.currentUser } returns user
        every { user.uid } returns userId
        every { user.email } returns email
    }

    @Test
    fun `fetchUserData returns User when Firestore document exists`() = runTest {
        val snapshot: DocumentSnapshot = mockk()
        val dto = FirestoreUserDTO(email = email)
        val expectedUser = dto.toDomain()

        val usersCollection = mockk<CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val userDataCollection = mockk<CollectionReference>()
        val profileDoc = mockk<DocumentReference>()
        val snapshotTask: Task<DocumentSnapshot> = mockk()

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(userId) } returns userDoc
        every { userDoc.collection("userData") } returns userDataCollection
        every { userDataCollection.document("profile") } returns profileDoc
        every { snapshotTask.isCanceled } returns false

        every { snapshot.toObject(FirestoreUserDTO::class.java) } returns dto

        every { snapshotTask.isComplete } returns true
        every { snapshotTask.isSuccessful } returns true
        every { snapshotTask.result } returns snapshot
        every { snapshotTask.exception } returns null

        coEvery { profileDoc.get() } returns snapshotTask

        val result = service.fetchUserData()

        assertEquals(expectedUser, result)
    }

    @Test
    fun `fetchUserData returns null when no FirebaseUser`() = runTest {
        every { auth.currentUser } returns null
        val result = service.fetchUserData()
        assertNull(result)
    }

    @Test
    fun `saveUserData sends data to Firestore with correct UID`() = runTest {
        val testUser = User(email = "")

        val usersCollection = mockk<CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val userDataCollection = mockk<CollectionReference>()
        val profileDoc = mockk<DocumentReference>()
        val setTask: Task<Void> = mockk()

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(userId) } returns userDoc
        every { userDoc.collection("userData") } returns userDataCollection
        every { userDataCollection.document("profile") } returns profileDoc

        // 🧩 важные моки для await()
        every { setTask.isComplete } returns true
        every { setTask.isSuccessful } returns true
        every { setTask.isCanceled } returns false
        every { setTask.exception } returns null
        every { setTask.result } returns null

        coEvery { profileDoc.set(any()) } returns setTask

        service.saveUserData(testUser)

        coVerify {
            profileDoc.set(match<FirestoreUserDTO> {
                it.email == email
            })
        }
    }


    @Test
    fun `saveUserData does nothing when no current user`() = runTest {
        every { auth.currentUser } returns null
        service.saveUserData(User())
        verify(exactly = 0) { firestore.collection("users") }
    }
}
