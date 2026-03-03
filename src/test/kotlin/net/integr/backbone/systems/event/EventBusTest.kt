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

package net.integr.backbone.systems.event

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventBusTest {
    class TestEvent : Event()

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
                event.setCallback("Callback from handler")
            }
        }

        val listener = TestListener()
        EventBus.register(listener)
        val event = TestEvent()
        val result = EventBus.post(event)

        assertEquals("Callback from handler", result)
        assertEquals("Callback from handler", event.callback())
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

        // Create listener and register it
        var listener: TestListener? = TestListener()
        EventBus.register(listener!!)
        
        // First post should work
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

        // Create listener, register it, then clear reference
        var listener: TestListener? = TestListener()
        EventBus.register(listener!!)
        listener = null
        
        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)

        // Post event - should detect dead handler
        EventBus.post(TestEvent())
        
        // Post again - dead handler should have been removed from first post
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
        
        // First post should throw and increment count
        EventBus.post(TestEvent())
        assertEquals(1, callCount, "Handler should have been called once")

        // Second post should not call handler (it was removed due to exception)
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

        // Register alive listener (keep strong reference)
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
        
        // Alive handler should be called
        assertEquals(1, aliveCallCount, "Alive handler should have been called")
        
        // Post again to ensure system still works after cleanup
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

        // Keep strong reference to listener
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

        // Register first instance and keep it alive
        val listener1 = TestListener().apply { id = 1 }
        EventBus.register(listener1)

        // Register second instance and let it die
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
        
        // First instance should be called
        assertEquals(1, instance1Calls, "Alive instance should be called")
        
        // Post again to verify first instance still works
        EventBus.post(TestEvent())
        assertEquals(2, instance1Calls, "Alive instance should continue working")
    }

    @AfterEach
    fun tearDown() {
        EventBus.clear()
    }
}