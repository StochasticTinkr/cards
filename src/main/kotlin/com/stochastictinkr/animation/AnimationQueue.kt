package com.stochastictinkr.animation

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Deque
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import javax.swing.Timer

class AnimationQueue(val clock: Clock) {
    private val oneSecond = TimeUnit.SECONDS.toMillis(1).toInt()
    private fun frequencyToMillis(value: Int) = oneSecond / value
    var framerate: Int = 30
        set(value) {
            field = value
            timer.delay = frequencyToMillis(value)
        }

    private val timer: Timer = Timer(0) { nextFrame() }

    private val queue: Deque<Transition> = LinkedList()
    private val now: Instant get() = clock.instant()
    private var activeTransition: ActiveTransition? = null

    private class ActiveTransition(val start: Instant, val transition: Transition) {
        val progressRate = 1.0 / transition.duration.toMillis()

        fun post(value: Double) {
            transition.action(value)
        }

        fun post(now: Instant): Boolean {
            val progress = start.until(now, ChronoUnit.MILLIS) * progressRate
            if (progress < 0) {
                throw IllegalStateException("Time travelled!")
            }
            if (progress >= 1.0) {
                post(1.0)
                return true
            }
            post(progress)
            return false
        }
    }

    fun add(transition: Transition) {
        queue.addLast(transition)
        timer.start()
    }

    init {
        with(timer) {
            isCoalesce = true
            isRepeats = true
        }
    }

    private fun nextFrame() {
        val currentTransition = activeTransition
        if (currentTransition == null || currentTransition.post(now)) {
            activeTransition = queue.pollFirst()?.let { ActiveTransition(now, it).apply { post(0.0) } }
        }
        if (activeTransition == null) {
            timer.stop()
        }
    }
}