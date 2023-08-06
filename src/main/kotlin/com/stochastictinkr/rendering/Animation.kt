package com.stochastictinkr.rendering

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.NavigableMap
import java.util.TreeMap

class Animation<O, S>(
    val clock: Clock = Clock.systemUTC(),
    private val interpolate: (S, Double, S) -> S,
) {
    private val objects: MutableMap<O, NavigableMap<Instant, S>> = LinkedHashMap()

    operator fun get(obj: O, instant: Instant = clock.instant()): S? =
        objects[obj]?.interpolated(instant)

    operator fun set(obj: O, instant: Instant = clock.instant(), state: S?) {
        if (state != null) {
            val map = objects.computeIfAbsent(obj) { TreeMap() }
            map[instant] = state
        } else {
            objects.compute(obj) { _, value ->
                value?.remove(instant)
                if (value.isNullOrEmpty()) null else value;
            }
        }
    }

    operator fun contains(obj: O) = objects.containsKey(obj)

    fun asSequence(at: Instant = clock.instant()): Sequence<Pair<O, S>> =
        objects.asSequence().map { (key, value) ->
            key to value.interpolated(at)
        }

    fun clear() {
        objects.clear()
    }

    inline fun forEach(at: Instant, consumer: (O, S) -> Unit) {
        asSequence(at).forEach { (obj, state) ->
            consumer(obj, state)
        }
    }

    inline fun forEach(consumer: (O, S) -> Unit) {
        asSequence().forEach { (obj, state) ->
            consumer(obj, state)
        }
    }


    private fun NavigableMap<Instant, S>.interpolated(time: Instant): S {
        val before = floorEntry(time)
        val after = ceilingEntry(time)
        return when {
            before === null && after == null -> throw IllegalStateException("No state at this time!")
            before === null -> after.value
            after === null -> before.value
            else -> {
                val (initialTime, initialState) = before
                val (endTime, endState) = after
                interpolate(initialState, (initialTime..endTime).percentage(time), endState)
            }
        }
    }

    private fun ClosedRange<Instant>.percentage(time: Instant): Double {
        val sinceLast = Duration.between(start, time).toNanos().toDouble()
        val untilNext = Duration.between(time, endInclusive).toNanos().toDouble()
        return sinceLast / (sinceLast + untilNext)
    }

    fun needsUpdate(instant: Instant = clock.instant()): Boolean =
        objects.any { (_, items) -> items.lastKey().isAfter(instant) }

    fun append(obj: O, delta: Duration, minimumTime: Instant = clock.instant(), state: (S?) -> S) {
        val map = objects.computeIfAbsent(obj) { TreeMap() }
        if (map.isEmpty()) {
            map[minimumTime] = state(null)
        } else {
            map[maxOf(map.lastKey(), minimumTime) + delta] = state(map.lastEntry().value)
        }
    }
}

