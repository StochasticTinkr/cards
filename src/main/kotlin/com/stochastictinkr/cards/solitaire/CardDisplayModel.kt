package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.skywing.awt.geom.makeTransform
import com.stochastictinkr.skywing.awt.geom.plus
import com.stochastictinkr.skywing.awt.geom.point
import com.stochastictinkr.skywing.awt.geom.roundRectangle
import com.stochastictinkr.skywing.awt.geom.times
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Point2D
import kotlin.math.abs
import kotlin.math.min

class CardDisplayModel(
    private val isSelected: (Card) -> Boolean,
    private val images: CardImages,
    private var getBack: () -> CardBacks,
) {
    private val back: CardBacks get() = getBack()

    class Position(val start: Point2D, val target: Point2D = start) {
        operator fun get(delta: Float) = start * (1 - delta) + target * delta
    }

    class Flip(val start: Float, val target: Float = start) {
        operator fun get(delta: Float) = start * (1 - delta) + target * delta
    }

    class CardDisplay(
        var position: Position,
        var flip: Flip,
        var delta: Float,
        var layer: Int = 0,
    ) {
        val point get() = position[delta]
        val flipAmount get() = flip[delta]
        fun update() {
            delta = min(delta + .05f, 1f)
        }

        fun setTarget(position: Point2D, visible: Boolean, layer: Int = this.layer) {
            this.position = Position(point, position)
            this.flip = if (visible) Flip(flipAmount, 1f) else Flip(0f, 0f)
            this.layer = layer
            delta = 0f
        }
    }

    val cards: MutableMap<Card, CardDisplay> = LinkedHashMap()

    operator fun get(card: Card) = cards.computeIfAbsent(card) { CardDisplay(Position(point(0.0, 0.0)), Flip(0f), 0f) }

    fun update(): Boolean {
        val needPaint = cards.values.any { it.delta < 1f }
        cards.values.forEach(CardDisplay::update)
        return needPaint
    }

    fun drawAll(g: Graphics2D) {
        cards.toList().sortedBy { (_, display) -> display.layer }.forEach { (card, display) -> draw(g, card, display) }
    }

    private fun draw(
        g: Graphics2D,
        card: Card,
        display: CardDisplay,
    ) {
        val point = display.point
        val flip = display.flipAmount
        val image = if (flip <= .5f) {
            images[back]
        } else {
            images[card]
        }

        val transform = makeTransform {
            translate(point.x, point.y + images.cardHeight * (1 - abs(.5 - flip) * 2))
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
}