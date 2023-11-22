package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.rendering.Animation
import com.stochastictinkr.skywing.geom.affineTransform
import com.stochastictinkr.skywing.geom.plus
import com.stochastictinkr.skywing.geom.roundRectangle
import com.stochastictinkr.skywing.geom.times
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Point2D
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

class CardDisplayModel(
    private val isSelected: (Card) -> Boolean,
    private val images: CardImages,
    clock: Clock = Clock.systemUTC(),
    private var getBack: () -> CardBacks,
) {
    private var lastPaint: Instant = clock.instant().minusSeconds(1)
    private val back: CardBacks get() = getBack()
    private val animation = Animation<Card, State>(clock) { left, delta, right ->
        left * (1.0 - delta) + right * delta
    }

    data class State(
        val position: Point2D,
        val flip: Float,
        val z: Float,
    ) {
        operator fun times(delta: Double) = State(position * delta, flip * delta.toFloat(), z * delta.toFloat())
        operator fun plus(state: State): State = State(position + state.position, flip + state.flip, z + state.z)
    }

    fun drawAll(g: Graphics2D) {
        lastPaint = Instant.now()
        animation.asSequence(lastPaint)
            .sortedBy { (_, state) -> state.z }
            .forEach { (card, state) -> draw(g, card, state) }
    }

    fun clear() {
        animation.clear()
    }

    private fun draw(
        g: Graphics2D,
        card: Card,
        state: State,
    ) {
        val position = state.position
        val flip = state.flip
        val image = if (flip <= .5f) {
            images[back]
        } else {
            images[card]
        }

        val transform = affineTransform {
            translate(position.x, position.y + images.cardHeight * (1 - abs(.5 - flip) * 2))
            scale(1.0, abs(flip - .5) * 2)
        }
        if (isSelected(card)) {
            g.paint = Color.YELLOW
            g.fill(
                transform.createTransformedShape(roundRectangle {
                    setRoundRect(
                        -1.0, -1.0,
                        images.cardWidth + 2.0, images.cardHeight + 2.0,
                        5.0, 5.0
                    )
                })
            )
        }
        g.drawRenderedImage(image, transform)
    }

    operator fun set(card: Card, value: State) {
        animation[card] = value
    }

    fun needsUpdate(): Boolean {
        return animation.needsUpdate(lastPaint)
    }

    operator fun get(card: Card): NextState = NextState(card)

    inner class NextState(val card: Card, var delta: Duration = Duration.ofMillis(125)) {
        fun setTarget(position: Point2D, visible: Boolean, z: Int) {
            val state = State(position, if (visible) 1f else 0f, z.toFloat())
            animation.append(card, Duration.ofMillis(1)) {
                it ?: state
            }
            animation.append(card, delta) { _: State? ->
                state
            }
        }
    }
}