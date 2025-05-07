@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.viewmodel

import android.graphics.Bitmap
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.camera.CameraScanResult
import com.hse.coursework.nutrik.service.BarcodeScannerService
import com.hse.coursework.nutrik.ui.theme.screen.camera.CameraViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val barcodeScanner: BarcodeScannerService = mockk()
    private lateinit var viewModel: CameraViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val fakeBitmap = mockk<Bitmap>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CameraViewModel(barcodeScanner)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onImageCaptured - Found result updates uiState correctly`() = runTest {
        val product = ProductEntity(id = "p1", name = "Milk")
        coEvery { barcodeScanner.analyze(fakeBitmap) } returns CameraScanResult.Found(product)

        viewModel.onImageCaptured(fakeBitmap)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.hasPhoto)
        assertEquals(fakeBitmap, viewModel.uiState.photoBitmap)
        assertFalse(viewModel.uiState.scanning)
        assertEquals(product, viewModel.uiState.foundProduct)
        assertNull(viewModel.uiState.message)
    }

    @Test
    fun `onImageCaptured - NotFound result updates uiState correctly`() = runTest {
        coEvery { barcodeScanner.analyze(fakeBitmap) } returns CameraScanResult.NotFound("not found!")

        viewModel.onImageCaptured(fakeBitmap)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.hasPhoto)
        assertEquals(fakeBitmap, viewModel.uiState.photoBitmap)
        assertFalse(viewModel.uiState.scanning)
        assertNull(viewModel.uiState.foundProduct)
        assertEquals("not found!", viewModel.uiState.message)
    }

    @Test
    fun `onImageCaptured - Error result updates uiState correctly`() = runTest {
        coEvery { barcodeScanner.analyze(fakeBitmap) } returns CameraScanResult.Error("fail!")

        viewModel.onImageCaptured(fakeBitmap)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.hasPhoto)
        assertEquals(fakeBitmap, viewModel.uiState.photoBitmap)
        assertFalse(viewModel.uiState.scanning)
        assertNull(viewModel.uiState.foundProduct)
        assertEquals("fail!", viewModel.uiState.message)
    }

    @Test
    fun `onRetake resets uiState`() {
        viewModel.onRetake()
        assertFalse(viewModel.uiState.hasPhoto)
        assertNull(viewModel.uiState.photoBitmap)
        assertFalse(viewModel.uiState.scanning)
        assertNull(viewModel.uiState.message)
        assertNull(viewModel.uiState.foundProduct)
    }
}
