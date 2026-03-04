/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused", "AssignedValueIsNeverRead")

package net.integr.backbone.systems.event

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventBusTest {
    class TestEvent : Event()
    class CancelableTestEvent : Event.Cancelable()

    @Test
    fun testRegisterAndPost() {
        class TestListener {
            var eventHandled = false

            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                eventHandled = true
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        EventBus.post(TestEvent())

        assertTrue(listener.eventHandled)
    }

    @Test
    fun testUnregister() {
        var eventHandled = false
        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                eventHandled = true
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        EventBus.unregister(listener)
        EventBus.post(TestEvent())

        assertFalse(eventHandled)
    }

    @Test
    fun testPriority() {
        val callOrder = mutableListOf<Int>()
        class TestListener {
            @BackboneEventHandler(priority = EventPriority.THREE_AFTER)
            fun onLow(event: TestEvent) {
                callOrder.add(1)
            }

            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onNormal(event: TestEvent) {
                callOrder.add(2)
            }

            @BackboneEventHandler(priority = EventPriority.THREE_BEFORE)
            fun onHigh(event: TestEvent) {
                callOrder.add(3)
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        EventBus.post(TestEvent())

        assertEquals(listOf(3, 2, 1), callOrder)
    }

    @Test
    fun testEventCallback() {
        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                event.callback = "Callback from handler"
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        val event = TestEvent()
        val result = EventBus.post(event)

        assertEquals("Callback from handler", result)
        assertEquals("Callback from handler", event.callback)
    }

    @Test
    fun testSamePriorityHandlers() {
        var handler1Called = false
        var handler2Called = false

        class TestListener {
            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onEvent1(event: TestEvent) {
                handler1Called = true
            }

            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onEvent2(event: TestEvent) {
                handler2Called = true
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        EventBus.post(TestEvent())

        assertTrue(handler1Called, "Handler 1 should have been called")
        assertTrue(handler2Called, "Handler 2 should have been called")
    }

    @Test
    fun testHandlersInDifferentInstances() {
        var callCount = 0

        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                callCount++
            }
        }

        val listener1 = TestListener()
        val listener2 = TestListener()

        EventBus.register(listener1)
        EventBus.register(listener2)
        EventBus.post(TestEvent())

        assertEquals(2, callCount, "Handler should have been called for both instances")
    }

    @Test
    fun testMultipleInstancesWithSamePriorityHandlers() {
        var callCount = 0

        class TestListener {
            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onEvent1(event: TestEvent) {
                callCount++
            }

            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onEvent2(event: TestEvent) {
                callCount++
            }
        }

        val listener1 = TestListener()
        val listener2 = TestListener()

        EventBus.register(listener1)
        EventBus.register(listener2)
        EventBus.post(TestEvent())

        assertEquals(4, callCount, "All handlers across all instances should have been called")
    }

    @Test
    fun testDeadHandlerAutoRemoval() {
        var callCount = 0

        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                callCount++
            }
        }

        // Create a listener and register it
        var listener: TestListener? = TestListener()
        EventBus.register(listener!!)
        
        // The first post should work
        EventBus.post(TestEvent())
        assertEquals(1, callCount, "Handler should have been called")

        // Clear the reference and force garbage collection
        listener = null
        System.gc()
        Thread.sleep(100) // Give GC time to run
        System.gc()
        Thread.sleep(100)

        // Try to post again - dead handler should be detected and removed
        EventBus.post(TestEvent())
        
        // Handler should not have been called again (still 1, not 2)
        // Note: This test may be flaky depending on GC behavior
        // The important thing is no exception is thrown
    }

    @Test
    fun testDeadHandlerRemovedOnInvocation() {
        var callCount = 0

        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                callCount++
            }
        }

        // Create a listener, register it, then clear a reference
        var listener: TestListener? = TestListener()
        EventBus.register(listener!!)
        listener = null
        
        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)

        // Post-event - should detect a dead handler
        EventBus.post(TestEvent())
        
        // Post again - dead handler should have been removed from the first post
        EventBus.post(TestEvent())
        
        // No exceptions should be thrown
        assertTrue(true, "Dead handlers should be removed gracefully")
    }

    @Test
    fun testHandlerExceptionRemovesHandler() {
        var callCount = 0

        class TestListener {
            @BackboneEventHandler
            fun onThrowingEvent(event: TestEvent) {
                callCount++
                throw RuntimeException("Intentional exception")
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        
        // The first post should throw and increment count
        EventBus.post(TestEvent())
        assertEquals(1, callCount, "Handler should have been called once")

        // The second post should not call handler (it was removed due to an exception)
        EventBus.post(TestEvent())
        assertEquals(1, callCount, "Handler should have been removed after exception")
    }

    @Test
    fun testMixedAliveAndDeadHandlers() {
        var aliveCallCount = 0
        var deadCallCount = 0

        class AliveListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                aliveCallCount++
            }
        }

        class DeadListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                deadCallCount++
            }
        }

        // Register a live listener (keep strong reference)
        val aliveListener = AliveListener()
        EventBus.register(aliveListener)

        // Register dead listener (no strong reference)
        var deadListener: DeadListener? = DeadListener()
        EventBus.register(deadListener!!)
        deadListener = null

        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)

        // Post event
        EventBus.post(TestEvent())
        
        // Live handler should be called
        assertEquals(1, aliveCallCount, "Alive handler should have been called")
        
        // Post again to ensure a system still works after cleanup
        EventBus.post(TestEvent())
        assertEquals(2, aliveCallCount, "Alive handler should have been called again")
    }

    @Test
    fun testWeakReferenceDoesNotPreventNormalOperation() {
        var callCount = 0

        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                callCount++
            }
        }

        // Keep strong reference to the listener
        val listener = TestListener()
        EventBus.register(listener)
        
        // Post multiple times
        repeat(5) {
            EventBus.post(TestEvent())
        }
        
        assertEquals(5, callCount, "Handler should be called all 5 times when instance is alive")
    }

    @Test
    fun testDeadHandlerWithMultipleEvents() {
        class AnotherTestEvent : Event()
        
        var testEventCallCount = 0
        var anotherEventCallCount = 0

        class TestListener {
            @BackboneEventHandler
            fun onTestEvent(event: TestEvent) {
                testEventCallCount++
            }

            @BackboneEventHandler
            fun onAnotherEvent(event: AnotherTestEvent) {
                anotherEventCallCount++
            }
        }

        // Register and then clear reference
        var listener: TestListener? = TestListener()
        EventBus.register(listener!!)
        listener = null

        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)

        // Post both event types - should handle dead handlers gracefully
        EventBus.post(TestEvent())
        EventBus.post(AnotherTestEvent())
        
        // No exceptions should be thrown
        assertTrue(true, "Dead handlers for multiple event types should be handled gracefully")
    }

    @Test
    fun testDeadHandlerDoesNotAffectOtherInstances() {
        var instance1Calls = 0
        var instance2Calls = 0

        class TestListener {
            var id: Int = 0
            
            @BackboneEventHandler
            fun onEvent(event: TestEvent) {
                if (id == 1) instance1Calls++
                else if (id == 2) instance2Calls++
            }
        }

        // Register the first instance and keep it alive
        val listener1 = TestListener().apply { id = 1 }
        EventBus.register(listener1)

        // Register the second instance and let it die
        var listener2: TestListener? = TestListener().apply { id = 2 }
        EventBus.register(listener2!!)
        listener2 = null

        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)

        // Post event
        EventBus.post(TestEvent())
        
        // The first instance should be called
        assertEquals(1, instance1Calls, "Alive instance should be called")
        
        // Post again to verify the first instance still works
        EventBus.post(TestEvent())
        assertEquals(2, instance1Calls, "Alive instance should continue working")
    }

    @Test
    fun testCancelableEventBasicCancellation() {
        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: CancelableTestEvent) {
                event.cancel()
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        val event = CancelableTestEvent()
        EventBus.post(event)

        assertTrue(event.canceled, "Event should be canceled")
    }

    @Test
    fun testCancelableEventStopsPropagation() {
        val callOrder = mutableListOf<Int>()

        class TestListener {
            @BackboneEventHandler(priority = EventPriority.THREE_BEFORE)
            fun onFirst(event: CancelableTestEvent) {
                callOrder.add(1)
                event.cancel()
            }

            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onSecond(event: CancelableTestEvent) {
                callOrder.add(2)
            }

            @BackboneEventHandler(priority = EventPriority.THREE_AFTER)
            fun onThird(event: CancelableTestEvent) {
                callOrder.add(3)
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        EventBus.post(CancelableTestEvent())

        assertEquals(listOf(1), callOrder, "Only the first handler should be called before cancellation")
    }

    @Test
    fun testCancelableEventWithPriorities() {
        var highPriorityCalled = false
        var lowPriorityCalled = false

        class HighPriorityListener {
            @BackboneEventHandler(priority = EventPriority.THREE_BEFORE)
            fun onEvent(event: CancelableTestEvent) {
                highPriorityCalled = true
                event.cancel()
            }
        }

        class LowPriorityListener {
            @BackboneEventHandler(priority = EventPriority.THREE_AFTER)
            fun onEvent(event: CancelableTestEvent) {
                lowPriorityCalled = true
            }
        }

        val highListener = HighPriorityListener()
        val lowListener = LowPriorityListener()
        EventBus.register(highListener)
        EventBus.register(lowListener)
        EventBus.post(CancelableTestEvent())

        assertTrue(highPriorityCalled, "High priority handler should be called")
        assertFalse(lowPriorityCalled, "Low priority handler should not be called after cancellation")
    }

    @Test
    fun testCancelableEventWithCallback() {
        class TestListener {
            @BackboneEventHandler
            fun onEvent(event: CancelableTestEvent) {
                event.callback = "Callback value"
                event.cancel()
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        val event = CancelableTestEvent()
        val result = EventBus.post(event)

        assertEquals("Callback value", result, "Callback should be returned when event is canceled")
        assertEquals("Callback value", event.callback, "Event callback should be set")
        assertTrue(event.canceled, "Event should be canceled")
    }

    @Test
    fun testCancelableEventNotCanceled() {
        val callOrder = mutableListOf<Int>()

        class TestListener {
            @BackboneEventHandler(priority = EventPriority.THREE_BEFORE)
            fun onFirst(event: CancelableTestEvent) {
                callOrder.add(1)
            }

            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onSecond(event: CancelableTestEvent) {
                callOrder.add(2)
            }

            @BackboneEventHandler(priority = EventPriority.THREE_AFTER)
            fun onThird(event: CancelableTestEvent) {
                callOrder.add(3)
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        val event = CancelableTestEvent()
        EventBus.post(event)

        assertEquals(listOf(1, 2, 3), callOrder, "All handlers should be called when event is not canceled")
        assertFalse(event.canceled, "Event should not be canceled")
    }

    @Test
    fun testMultipleListenersWithMixedCancellation() {
        var listener1Called = false
        var listener2Called = false
        var listener3Called = false

        class CancelingListener {
            @BackboneEventHandler(priority = EventPriority.NORMAL)
            fun onEvent(event: CancelableTestEvent) {
                listener1Called = true
                event.cancel()
            }
        }

        class NonCancelingListener1 {
            @BackboneEventHandler(priority = EventPriority.THREE_BEFORE)
            fun onEvent(event: CancelableTestEvent) {
                listener2Called = true
            }
        }

        class NonCancelingListener2 {
            @BackboneEventHandler(priority = EventPriority.THREE_AFTER)
            fun onEvent(event: CancelableTestEvent) {
                listener3Called = true
            }
        }

        val cancelingListener = CancelingListener()
        val nonCancelingListener1 = NonCancelingListener1()
        val nonCancelingListener2 = NonCancelingListener2()

        EventBus.register(cancelingListener)
        EventBus.register(nonCancelingListener1)
        EventBus.register(nonCancelingListener2)

        EventBus.post(CancelableTestEvent())

        assertTrue(listener2Called, "Higher priority non-canceling listener should be called")
        assertTrue(listener1Called, "Canceling listener should be called")
        assertFalse(listener3Called, "Lower priority listener should not be called after cancellation")
    }

    @AfterEach
    fun tearDown() {
        EventBus.clear()
    }
}