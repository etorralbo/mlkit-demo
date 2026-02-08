package com.mlkit.demo.viewmodel

import app.cash.turbine.test
import com.mlkit.demo.model.BoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoViewModelTest {

    private lateinit var viewModel: DemoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DemoViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be null`() = runTest {
        viewModel.detectionState.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `onObjectDetected should emit detection state`() = runTest {
        val boundingBox = BoundingBox(10f, 20f, 100f, 200f)

        viewModel.detectionState.test {
            assertNull(awaitItem()) // Initial state

            viewModel.onObjectDetected("Phone", 0.87f, boundingBox)
            advanceTimeBy(100) // Small delay for emission

            val state = awaitItem()
            assertNotNull(state)
            assertEquals("Phone", state?.label)
            assertEquals(0.87f, state?.confidence)
            assertEquals(boundingBox, state?.boundingBox)
        }
    }

    @Test
    fun `detection should display for 4 seconds`() = runTest {
        val boundingBox = BoundingBox(10f, 20f, 100f, 200f)

        viewModel.detectionState.test {
            assertNull(awaitItem()) // Initial state

            viewModel.onObjectDetected("Laptop", 0.92f, boundingBox)
            advanceTimeBy(100)

            val state = awaitItem()
            assertNotNull(state)
            assertEquals("Laptop", state?.label)

            // After 3.9 seconds, state should still be present
            advanceTimeBy(3900)
            expectNoEvents()

            // After 4 seconds, state should be cleared
            advanceTimeBy(100)
            assertNull(awaitItem())
        }
    }

    @Test
    fun `detection should have 1 second cooldown after display`() = runTest {
        val boundingBox1 = BoundingBox(10f, 20f, 100f, 200f)
        val boundingBox2 = BoundingBox(30f, 40f, 120f, 220f)

        viewModel.detectionState.test {
            assertNull(awaitItem()) // Initial state

            // First detection
            viewModel.onObjectDetected("Phone", 0.87f, boundingBox1)
            advanceTimeBy(100)
            assertNotNull(awaitItem())

            // After 4 seconds, cleared
            advanceTimeBy(3900)
            assertNull(awaitItem())

            // Try to detect during cooldown (should be ignored)
            advanceTimeBy(500) // 0.5s into cooldown
            viewModel.onObjectDetected("Laptop", 0.92f, boundingBox2)
            advanceTimeBy(100)
            expectNoEvents() // Should be ignored

            // After full cooldown (total 5 seconds from first detection)
            advanceTimeBy(500) // Complete the cooldown

            // Now new detection should work
            viewModel.onObjectDetected("Laptop", 0.92f, boundingBox2)
            advanceTimeBy(100)
            val state = awaitItem()
            assertNotNull(state)
            assertEquals("Laptop", state?.label)
        }
    }

    @Test
    fun `detections during cycle should be ignored`() = runTest {
        val boundingBox1 = BoundingBox(10f, 20f, 100f, 200f)
        val boundingBox2 = BoundingBox(30f, 40f, 120f, 220f)

        viewModel.detectionState.test {
            assertNull(awaitItem()) // Initial state

            // First detection
            viewModel.onObjectDetected("Phone", 0.87f, boundingBox1)
            advanceTimeBy(100)
            val firstState = awaitItem()
            assertEquals("Phone", firstState?.label)

            // Try to detect new object after 2 seconds (during display)
            advanceTimeBy(2000)
            viewModel.onObjectDetected("Laptop", 0.92f, boundingBox2)
            advanceTimeBy(100)
            expectNoEvents() // Should be ignored, still showing Phone

            // Original detection continues
            advanceTimeBy(1900)
            assertNull(awaitItem()) // Cleared after 4 seconds total
        }
    }

    @Test
    fun `clearDetection should clear state and stop cycle`() = runTest {
        val boundingBox = BoundingBox(10f, 20f, 100f, 200f)

        viewModel.detectionState.test {
            assertNull(awaitItem()) // Initial state

            viewModel.onObjectDetected("Phone", 0.87f, boundingBox)
            advanceTimeBy(100)
            assertNotNull(awaitItem())

            // Clear detection manually
            viewModel.clearDetection()
            advanceTimeBy(100)
            assertNull(awaitItem())

            // New detection should work immediately (cycle stopped)
            viewModel.onObjectDetected("Laptop", 0.92f, boundingBox)
            advanceTimeBy(100)
            val state = awaitItem()
            assertNotNull(state)
            assertEquals("Laptop", state?.label)
        }
    }

    @Test
    fun `multiple detections after cooldown should work correctly`() = runTest {
        val boundingBox1 = BoundingBox(10f, 20f, 100f, 200f)
        val boundingBox2 = BoundingBox(30f, 40f, 120f, 220f)

        viewModel.detectionState.test {
            assertNull(awaitItem()) // Initial state

            // First detection cycle
            viewModel.onObjectDetected("Phone", 0.87f, boundingBox1)
            advanceTimeBy(100)
            assertEquals("Phone", awaitItem()?.label)

            advanceTimeBy(3900)
            assertNull(awaitItem()) // Cleared after 4s

            // Complete cooldown + small buffer
            advanceTimeBy(1100)

            // Second detection cycle
            viewModel.onObjectDetected("Laptop", 0.92f, boundingBox2)
            advanceTimeBy(100)
            assertEquals("Laptop", awaitItem()?.label)

            advanceTimeBy(3900)
            assertNull(awaitItem()) // Cleared after 4s
        }
    }

    @Test
    fun `cancelling detection job should not cause errors`() = runTest {
        val boundingBox = BoundingBox(10f, 20f, 100f, 200f)

        viewModel.onObjectDetected("Phone", 0.87f, boundingBox)
        advanceTimeBy(100)

        // Clear detection (which cancels the job)
        viewModel.clearDetection()
        advanceTimeBy(100)

        // No errors should occur - test passes if this completes
        assertTrue(true)
    }
}
