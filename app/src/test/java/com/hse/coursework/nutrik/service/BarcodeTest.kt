@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.hse.coursework.nutrik.service

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.hse.coursework.nutrik.model.ProductEntity
import com.hse.coursework.nutrik.model.camera.CameraScanResult
import com.hse.coursework.nutrik.repository.product.ProductRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeScannerServiceLogicTest {

    private val productRepo: ProductRepository = mockk()
    private lateinit var barcodeScanner: BarcodeScannerService
    private val fakeClient: BarcodeScanner = mockk(relaxed = true)
    private val fakeTextClient: TextRecognizer = mockk(relaxed = true)
    private val fakeBitmap = mockk<Bitmap>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(InputImage::class)
        every { InputImage.fromBitmap(fakeBitmap, 0) } returns mockk()
        barcodeScanner = BarcodeScannerService(productRepo, fakeClient, fakeTextClient)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `analyze returns Found when barcode product found`() = runTest {
        val product = ProductEntity(id = "1", name = "From Barcode")
        val barcodeMock = mockk<Barcode>()
        val image = mockk<InputImage>()

        mockkStatic(InputImage::class)
        every { InputImage.fromBitmap(any(), 0) } returns image

        coEvery { fakeClient.process(any<InputImage>()) } returns Tasks.forResult(listOf(barcodeMock))
        every { barcodeMock.rawValue } returns "123456"
        coEvery { productRepo.getProductByBarcode("123456") } returns flowOf(product)

        val result = barcodeScanner.analyze(fakeBitmap)
        assertEquals(CameraScanResult.Found(product), result)
    }

    @Test
    fun `analyze falls back to text when barcode processing throws`() = runTest {
        every { fakeClient.process(any<InputImage>()) }
            .returns(Tasks.forException(RuntimeException("oops")))

        val tx = mockk<Text>()
        every { tx.text } returns "foundName"
        every { fakeTextClient.process(any<InputImage>()) }
            .returns(Tasks.forResult(tx))

        val product = ProductEntity("x", name = "ByName")
        every { productRepo.getProductByName("foundName") } returns flowOf(product)

        val result = barcodeScanner.analyze(fakeBitmap)
        assertEquals(CameraScanResult.Found(product), result)
    }

    @Test
    fun `analyze returns NotFound when both barcode and text empty`() = runTest {
        every { fakeClient.process(any<InputImage>()) }
            .returns(Tasks.forResult(emptyList()))
        val tx = mockk<Text>()
        every { tx.text } returns ""
        every { fakeTextClient.process(any<InputImage>()) }
            .returns(Tasks.forResult(tx))

        val result = barcodeScanner.analyze(fakeBitmap)
        assertEquals(CameraScanResult.NotFound("Продукт не найден"), result)
    }

    @Test
    fun `analyze returns NotFound when text found but no product`() = runTest {
        every { fakeClient.process(any<InputImage>()) }
            .returns(Tasks.forResult(emptyList()))
        val tx = mockk<Text>()
        every { tx.text } returns "   \ngood\n"
        every { fakeTextClient.process(any<InputImage>()) }
            .returns(Tasks.forResult(tx))
        every { productRepo.getProductByName("good") } returns flowOf(null)

        val result = barcodeScanner.analyze(fakeBitmap)
        assertEquals(
            CameraScanResult.NotFound("Продукт не найден по названию"),
            result
        )
    }

    @Test
    fun `analyze returns Error when text recognizer throws`() = runTest {
        every { fakeClient.process(any<InputImage>()) }
            .returns(Tasks.forResult(emptyList()))
        every { fakeTextClient.process(any<InputImage>()) }
            .returns(Tasks.forException(RuntimeException("ocr fail")))

        val result = barcodeScanner.analyze(fakeBitmap)
        assertEquals(
            CameraScanResult.Error("Не удалось распознать товар, попробуйте снова"),
            result
        )
    }

    @Test
    fun `sanitizeBarcode returns RegNumber if present`() {
        val result = barcodeScanner.run {
            val method = this::class.java.getDeclaredMethod("sanitizeBarcode", String::class.java)
            method.isAccessible = true
            method.invoke(this, "https://example.com?RegNumber=ABC123")
        }
        assertEquals("https://example.com?RegNumber=ABC123", result)
    }

    @Test
    fun `sanitizeBarcode returns original if no RegNumber`() {
        val input = "https://example.com?foo=bar"
        val result = barcodeScanner.run {
            val method = this::class.java.getDeclaredMethod("sanitizeBarcode", String::class.java)
            method.isAccessible = true
            method.invoke(this, input)
        }
        assertEquals(input, result)
    }

    @Test
    fun `sanitizeBarcode returns code as is if not a url`() {
        val input = "justtext123"
        val result = barcodeScanner.run {
            val method = this::class.java.getDeclaredMethod("sanitizeBarcode", String::class.java)
            method.isAccessible = true
            method.invoke(this, input)
        }
        assertEquals(input, result)
    }


    @Test
    fun `searchProductByCode returns ProductEntity if found`() = runTest {
        coEvery { productRepo.getProductByBarcode("123456") } returns flowOf(
            ProductEntity(id = "id", code = "123456", name = "Test")
        )

        val method = barcodeScanner::class.java.getDeclaredMethod(
            "searchProductByCode",
            String::class.java,
            Continuation::class.java
        )
        method.isAccessible = true

        var result: Any? = ProductEntity(id = "id", code = "123456", name = "Test")
        val continuation = object : Continuation<ProductEntity?> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(res: Result<ProductEntity?>) {
                result = res.getOrNull()
            }
        }

        method.invoke(barcodeScanner, "123456", continuation)

        assertEquals(
            ProductEntity(id = "id", code = "123456", name = "Test"),
            result
        )
    }

    @Test
    fun `searchProductByCode returns null if not found`() = runTest {
        coEvery { productRepo.getProductByBarcode("321") } returns flowOf(null)

        val method = barcodeScanner::class.java.getDeclaredMethod(
            "searchProductByCode",
            String::class.java,
            Continuation::class.java
        )
        method.isAccessible = true

        var result: Any? = null
        val continuation = object : Continuation<ProductEntity?> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(res: Result<ProductEntity?>) {
                result = res.getOrNull()
            }
        }

        method.invoke(barcodeScanner, "321", continuation)

        assertEquals(null, result)
    }


    @Test
    fun `searchProductByName returns ProductEntity if found`() = runTest {
        coEvery { productRepo.getProductByName("milk") } returns flowOf(
            ProductEntity(id = "id", name = "milk")
        )

        val method = barcodeScanner::class.java.getDeclaredMethod(
            "searchProductByName",
            String::class.java,
            Continuation::class.java
        )
        method.isAccessible = true

        var result: Any? = ProductEntity(id = "id", name = "milk")
        val cont = object : Continuation<ProductEntity?> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(res: Result<ProductEntity?>) {
                result = res.getOrDefault(ProductEntity(id = "id", name = "milk"))
            }
        }

        method.invoke(barcodeScanner, "milk", cont)

        assertEquals(
            ProductEntity(id = "id", name = "milk"),
            result
        )
    }

    @Test
    fun `searchProductByName returns null if not found`() = runTest {

        coEvery { productRepo.getProductByName("banana") } returns flowOf(null)

        val method = barcodeScanner::class.java.getDeclaredMethod(
            "searchProductByName",
            String::class.java,
            Continuation::class.java
        )
        method.isAccessible = true

        var result: Any? = null
        val cont = object : Continuation<ProductEntity?> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(res: Result<ProductEntity?>) {
                if (res.isSuccess) {
                    result = res.getOrNull()
                } else {
                    throw res.exceptionOrNull()!!
                }
            }
        }

        method.invoke(barcodeScanner, "banana", cont)

        assertEquals(null, result)
    }


    @Test
    fun `sanitizeBarcode returns RegNumber if present in url`() {
        val result = barcodeScanner.run {
            val method = this::class.java.getDeclaredMethod("sanitizeBarcode", String::class.java)
            method.isAccessible = true
            method.invoke(this, "https://site.com?RegNumber=123456") as String
        }
        assertEquals("https://site.com?RegNumber=123456", result)
    }

    @Test
    fun `sanitizeBarcode returns code if url has no RegNumber`() {
        val result = barcodeScanner.run {
            val method = this::class.java.getDeclaredMethod("sanitizeBarcode", String::class.java)
            method.isAccessible = true
            method.invoke(this, "https://site.com?foo=bar") as String
        }
        assertEquals("https://site.com?foo=bar", result)
    }

    @Test
    fun `sanitizeBarcode returns code as is if not url`() {
        val result = barcodeScanner.run {
            val method = this::class.java.getDeclaredMethod("sanitizeBarcode", String::class.java)
            method.isAccessible = true
            method.invoke(this, "simplebarcode") as String
        }
        assertEquals("simplebarcode", result)
    }
}
