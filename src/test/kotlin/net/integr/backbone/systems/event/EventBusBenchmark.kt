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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

/**
 * Comprehensive benchmark suite for the EventBus system.
 * 
 * These benchmarks measure:
 * - Event posting performance
 * - Handler registration/unregistration performance
 * - Scalability with many handlers
 * - Dead handler cleanup overhead
 * - Priority sorting overhead
 * - Different event types and handler complexities
 * 
 * @since 1.2.0
 */
class EventBusBenchmark {
    
    // Test Events
    class SimpleEvent : Event()
    class MediumEvent : Event()
    class ComplexEvent : Event() {
        var data: String = "benchmark_data"
        var counter: Int = 0
        var timestamp: Long = System.currentTimeMillis()
    }
    
    // Simple Listener
    class SimpleListener {
        var callCount = 0
        
        @BackboneEventHandler
        fun onSimpleEvent(event: SimpleEvent) {
            callCount++
        }
    }
    
    // Multi-Handler Listener
    class MultiHandlerListener {
        var simpleCount = 0
        var mediumCount = 0
        var complexCount = 0
        
        @BackboneEventHandler
        fun onSimpleEvent(event: SimpleEvent) {
            simpleCount++
        }
        
        @BackboneEventHandler
        fun onMediumEvent(event: MediumEvent) {
            mediumCount++
        }
        
        @BackboneEventHandler
        fun onComplexEvent(event: ComplexEvent) {
            complexCount++
            event.counter++
        }
    }
    
    // Listener with Different Priorities
    class PriorityListener {
        var lowCount = 0
        var normalCount = 0
        var highCount = 0
        
        @BackboneEventHandler(priority = EventPriority.THREE_AFTER)
        fun onLowPriorityEvent(event: SimpleEvent) {
            lowCount++
        }
        
        @BackboneEventHandler(priority = EventPriority.NORMAL)
        fun onNormalPriorityEvent(event: MediumEvent) {
            normalCount++
        }
        
        @BackboneEventHandler(priority = EventPriority.THREE_BEFORE)
        fun onHighPriorityEvent(event: ComplexEvent) {
            highCount++
        }
    }
    
    // Heavy Computation Listener
    class HeavyListener {
        @BackboneEventHandler
        fun onHeavyEvent(event: SimpleEvent) {
            // Simulate some work
            var sum = 0
            for (i in 1..100) {
                sum += i
            }
        }
    }
    
    @BeforeEach
    fun setUp() {
        EventBus.clear()
    }
    
    @AfterEach
    fun tearDown() {
        EventBus.clear()
    }
    
    // ============================================
    // BASIC PERFORMANCE BENCHMARKS
    // ============================================
    
    @Test
    fun benchmarkSingleHandlerSingleEvent() {
        val listener = SimpleListener()
        EventBus.register(listener)
        
        val iterations = 100_000
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                EventBus.post(SimpleEvent())
            }
        }
        
        println("Single handler, single event:")
        println("  Total time: ${timeMs}ms")
        println("  Iterations: $iterations")
        println("  Avg per event: ${timeMs.toDouble() / iterations}ms")
        println("  Events/sec: ${(iterations / (timeMs.toDouble() / 1000)).toLong()}")
        println("  Calls: ${listener.callCount}")
    }
    
    @Test
    fun benchmarkMultipleHandlersSingleEvent() {
        val listeners = List(10) { SimpleListener() }
        listeners.forEach { EventBus.register(it) }
        
        val iterations = 50_000
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                EventBus.post(SimpleEvent())
            }
        }
        
        println("10 handlers, single event:")
        println("  Total time: ${timeMs}ms")
        println("  Iterations: $iterations")
        println("  Avg per event: ${timeMs.toDouble() / iterations}ms")
        println("  Events/sec: ${(iterations / (timeMs.toDouble() / 1000)).toLong()}")
        println("  Total handler invocations: ${listeners.sumOf { it.callCount }}")
    }
    
    @Test
    fun benchmarkScalabilityManyHandlers() {
        val handlerCounts = listOf(1, 5, 10, 50, 100, 500)
        val iterations = 10_000
        
        println("Scalability test (many handlers):")
        println("  Iterations per test: $iterations")
        println()
        
        for (count in handlerCounts) {
            EventBus.clear()
            val listeners = List(count) { SimpleListener() }
            listeners.forEach { EventBus.register(it) }
            
            val timeMs = measureTimeMillis {
                repeat(iterations) {
                    EventBus.post(SimpleEvent())
                }
            }
            
            val avgPerEvent = timeMs.toDouble() / iterations
            val eventsPerSec = (iterations / (timeMs.toDouble() / 1000)).toLong()
            
            println("  $count handlers: ${timeMs}ms total, ${String.format("%.4f", avgPerEvent)}ms/event, $eventsPerSec events/sec")
        }
    }
    
    @Test
    fun benchmarkMultipleEventTypes() {
        val listener = MultiHandlerListener()
        EventBus.register(listener)
        
        val iterations = 30_000
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                EventBus.post(SimpleEvent())
                EventBus.post(MediumEvent())
                EventBus.post(ComplexEvent())
            }
        }
        
        println("Multiple event types (3 types):")
        println("  Total time: ${timeMs}ms")
        println("  Iterations: ${iterations * 3} (${iterations} of each type)")
        println("  Avg per event: ${timeMs.toDouble() / (iterations * 3)}ms")
        println("  Events/sec: ${((iterations * 3) / (timeMs.toDouble() / 1000)).toLong()}")
        println("  Simple: ${listener.simpleCount}, Medium: ${listener.mediumCount}, Complex: ${listener.complexCount}")
    }
    
    // ============================================
    // REGISTRATION/UNREGISTRATION BENCHMARKS
    // ============================================
    
    @Test
    fun benchmarkRegistration() {
        val count = 1_000
        val listeners = List(count) { SimpleListener() }
        
        val timeMs = measureTimeMillis {
            listeners.forEach { EventBus.register(it) }
        }
        
        println("Registration performance:")
        println("  Registered: $count listeners")
        println("  Total time: ${timeMs}ms")
        println("  Avg per registration: ${timeMs.toDouble() / count}ms")
    }
    
    @Test
    fun benchmarkUnregistration() {
        val count = 1_000
        val listeners = List(count) { SimpleListener() }
        listeners.forEach { EventBus.register(it) }
        
        val timeMs = measureTimeMillis {
            listeners.forEach { EventBus.unregister(it) }
        }
        
        println("Unregistration performance:")
        println("  Unregistered: $count listeners")
        println("  Total time: ${timeMs}ms")
        println("  Avg per unregistration: ${timeMs.toDouble() / count}ms")
    }
    
    @Test
    fun benchmarkChurnRegistrationUnregistration() {
        val iterations = 100
        
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                val listener = SimpleListener()
                EventBus.register(listener)
                EventBus.post(SimpleEvent())
                EventBus.unregister(listener)
            }
        }
        
        println("Churn test (register -> post -> unregister):")
        println("  Iterations: $iterations")
        println("  Total time: ${timeMs}ms")
        println("  Avg per cycle: ${timeMs.toDouble() / iterations}ms")
    }
    
    // ============================================
    // PRIORITY BENCHMARKS
    // ============================================
    
    @Test
    fun benchmarkPrioritySorting() {
        val listeners = List(20) { PriorityListener() }
        listeners.forEach { EventBus.register(it) }
        
        val iterations = 10_000
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                EventBus.post(SimpleEvent())
                EventBus.post(MediumEvent())
                EventBus.post(ComplexEvent())
            }
        }
        
        println("Priority sorting overhead:")
        println("  Listeners: ${listeners.size} (each with 3 different priority handlers)")
        println("  Total events: ${iterations * 3}")
        println("  Total time: ${timeMs}ms")
        println("  Avg per event: ${timeMs.toDouble() / (iterations * 3)}ms")
    }
    
    // ============================================
    // DEAD HANDLER BENCHMARKS
    // ============================================
    
    @Test
    fun benchmarkDeadHandlerDetection() {
        // Register handlers and let them become dead
        val liveListeners = List(5) { SimpleListener() }
        liveListeners.forEach { EventBus.register(it) }
        
        // Register dead handlers
        repeat(10) {
            val deadListener = SimpleListener()
            EventBus.register(deadListener)
            // Let it go out of scope
        }
        
        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)
        
        val iterations = 10_000
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                EventBus.post(SimpleEvent())
            }
        }
        
        println("Dead handler detection overhead:")
        println("  Live handlers: ${liveListeners.size}")
        println("  Dead handlers: ~10 (GC-dependent)")
        println("  Iterations: $iterations")
        println("  Total time: ${timeMs}ms")
        println("  Avg per event: ${timeMs.toDouble() / iterations}ms")
        println("  Total live calls: ${liveListeners.sumOf { it.callCount }}")
    }
    
    @Test
    fun benchmarkDeadHandlerCleanup() {
        // Create many dead handlers
        repeat(100) {
            val listener = SimpleListener()
            EventBus.register(listener)
        }
        
        // Force GC
        System.gc()
        Thread.sleep(100)
        System.gc()
        Thread.sleep(100)
        
        // First post triggers cleanup
        val firstPostTime = measureNanoTime {
            EventBus.post(SimpleEvent())
        }
        
        // Subsequent posts should be faster
        val subsequentTimes = mutableListOf<Long>()
        repeat(1000) {
            val time = measureNanoTime {
                EventBus.post(SimpleEvent())
            }
            subsequentTimes.add(time)
        }
        
        val avgSubsequent = subsequentTimes.average()
        
        println("Dead handler cleanup impact:")
        println("  First post (with cleanup): ${firstPostTime / 1_000}µs")
        println("  Avg subsequent posts: ${String.format("%.2f", avgSubsequent / 1_000)}µs")
        println("  Cleanup overhead: ${String.format("%.2f", (firstPostTime - avgSubsequent) / 1_000)}µs")
    }
    
    // ============================================
    // REAL-WORLD SCENARIO BENCHMARKS
    // ============================================
    
    @Test
    fun benchmarkMixedWorkload() {
        // Simulate a realistic mixed workload
        val simpleListeners = List(10) { SimpleListener() }
        val multiListeners = List(5) { MultiHandlerListener() }
        val heavyListeners = List(3) { HeavyListener() }
        
        simpleListeners.forEach { EventBus.register(it) }
        multiListeners.forEach { EventBus.register(it) }
        heavyListeners.forEach { EventBus.register(it) }
        
        val iterations = 5_000
        val timeMs = measureTimeMillis {
            repeat(iterations) {
                EventBus.post(SimpleEvent())
                if (it % 2 == 0) EventBus.post(MediumEvent())
                if (it % 5 == 0) EventBus.post(ComplexEvent())
            }
        }
        
        val totalEvents = iterations + (iterations / 2) + (iterations / 5)
        
        println("Mixed workload simulation:")
        println("  Simple listeners: ${simpleListeners.size}")
        println("  Multi listeners: ${multiListeners.size}")
        println("  Heavy listeners: ${heavyListeners.size}")
        println("  Total events posted: $totalEvents")
        println("  Total time: ${timeMs}ms")
        println("  Avg per event: ${timeMs.toDouble() / totalEvents}ms")
        println("  Events/sec: ${(totalEvents / (timeMs.toDouble() / 1000)).toLong()}")
    }
    
    @Test
    fun benchmarkHighFrequencyBursts() {
        val listener = SimpleListener()
        EventBus.register(listener)
        
        val bursts = 10
        val eventsPerBurst = 10_000
        val results = mutableListOf<Long>()
        
        println("High-frequency burst test:")
        println("  Bursts: $bursts")
        println("  Events per burst: $eventsPerBurst")
        println()
        
        repeat(bursts) { burstNum ->
            val timeMs = measureTimeMillis {
                repeat(eventsPerBurst) {
                    EventBus.post(SimpleEvent())
                }
            }
            results.add(timeMs)
            println("  Burst ${burstNum + 1}: ${timeMs}ms, ${(eventsPerBurst / (timeMs.toDouble() / 1000)).toLong()} events/sec")
        }
        
        println()
        println("  Avg burst time: ${results.average()}ms")
        println("  Min: ${results.minOrNull()}ms, Max: ${results.maxOrNull()}ms")
        println("  Total calls: ${listener.callCount}")
    }
    
    // ============================================
    // MEMORY AND OVERHEAD BENCHMARKS
    // ============================================
    
    @Test
    fun benchmarkMemoryOverhead() {
        val runtime = Runtime.getRuntime()
        
        // Baseline memory
        System.gc()
        Thread.sleep(100)
        val baselineMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Register many handlers
        val listeners = List(1000) { MultiHandlerListener() }
        listeners.forEach { EventBus.register(it) }
        
        System.gc()
        Thread.sleep(100)
        val afterRegistrationMemory = runtime.totalMemory() - runtime.freeMemory()
        
        val memoryUsed = (afterRegistrationMemory - baselineMemory) / 1024 / 1024
        
        println("Memory overhead estimation:")
        println("  Baseline memory: ${baselineMemory / 1024 / 1024}MB")
        println("  After registering 1000 listeners: ${afterRegistrationMemory / 1024 / 1024}MB")
        println("  Memory used by handlers: ~${memoryUsed}MB")
        println("  Avg per listener: ~${memoryUsed.toDouble() / 1000}MB")
    }
    
    @Test
    fun benchmarkConcurrentPosting() {
        val listener = SimpleListener()
        EventBus.register(listener)
        
        val threads = 4
        val iterationsPerThread = 25_000
        
        val timeMs = measureTimeMillis {
            val threadList = List(threads) {
                Thread {
                    repeat(iterationsPerThread) {
                        EventBus.post(SimpleEvent())
                    }
                }
            }
            
            threadList.forEach { it.start() }
            threadList.forEach { it.join() }
        }
        
        val totalEvents = threads * iterationsPerThread
        
        println("Concurrent posting test:")
        println("  Threads: $threads")
        println("  Events per thread: $iterationsPerThread")
        println("  Total events: $totalEvents")
        println("  Total time: ${timeMs}ms")
        println("  Events/sec: ${(totalEvents / (timeMs.toDouble() / 1000)).toLong()}")
        println("  Handler calls: ${listener.callCount}")
    }
}
