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

    @AfterEach
    fun tearDown() {
        EventBus.clear()
    }
}