package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.DeckDefinition
import com.stochastictinkr.skywing.awt.geom.size
import com.stochastictinkr.skywing.awt.hints
import com.stochastictinkr.skywing.rendering.geom.component1
import com.stochastictinkr.skywing.rendering.geom.component2
import org.apache.batik.gvt.GraphicsNode
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Dimension2D
import javax.swing.JComponent
import kotlin.math.ceil

private val Dimension2D.aspectRatio get() = width / height
private val DeckDefinition.cardSize get() = cards.first().image.bounds.size


class SolitaireComponent(val solitaireModel: SolitaireModel) : JComponent() {
    val foundationMargin = 11
    val tableauMargin = 6
    val foundationY = 4
    val tableauY: Int get() = foundationY + cardHeight + foundationMargin + tableauMargin
    val cardAspect: Double get() = solitaireModel.deck.cardSize.aspectRatio
    val cardWidth: Int get() = width / 9
    val cardHeight: Int get() = ceil(cardWidth / cardAspect).toInt()
    val tableauHiddenCardFanHeight get() = cardHeight / 32
    val tableauVisibleCardFanHeight get() = cardHeight / 8
    val stockFanHeight get() = cardHeight / 64

    override fun paintComponent(g: Graphics) {
        require(g is Graphics2D)
        g.hints {
            renderingQuality()
            antialiasingOn()
        }
        g.color = Color.GREEN.darker()
        g.fillRect(0, 0, width, height)
        solitaireModel.foundations.forEachIndexed { index, pile ->
            g.color = Color.BLACK
            val position = Rectangle(15 + (cardWidth + foundationMargin) * index, foundationY, cardWidth, cardHeight)
            g.draw(position)
            pile.cards.lastOrNull()?.let { visibleCard ->
                g.drawImage(position, visibleCard.image)
            }
        }

        solitaireModel.tableauPiles.forEachIndexed { index, pile ->
            val position = Rectangle(8 + (cardWidth + tableauMargin) * index, tableauY, cardWidth, cardHeight)
            g.color = Color.BLACK
            g.draw(position)
            pile.hiddenCards.forEach {
                g.drawImage(position, solitaireModel.deck.backImage)
                position.y += tableauHiddenCardFanHeight
            }
            pile.visibleCards.forEach {
                g.drawImage(position, it.image)
                position.y += tableauVisibleCardFanHeight
            }
        }

        run {
            val position = Rectangle(width - cardWidth - 8, tableauY, cardWidth, cardHeight)
            g.color = Color.BLACK
            g.draw(position)
            repeat(solitaireModel.stock.cards.size) {
                g.drawImage(position, solitaireModel.deck.backImage)
                position.y += stockFanHeight
            }
        }

        run {
            val position = Rectangle(width - cardWidth - 8, tableauY + cardHeight + 10, cardWidth, cardHeight)
            g.color = Color.BLACK
            g.draw(position)
            solitaireModel.wastePile.cards.lastOrNull()?.let {
                g.drawImage(position, it.image)
            }
        }
    }

    private fun Graphics2D.drawImage(position: Rectangle, image: GraphicsNode) {
        val g = create(position.x, position.y, position.width, position.height) as Graphics2D
        try {
            val (w, h) = image.bounds.size
            g.scale(1/(w.toDouble() / position.width), 1/(h / position.height.toDouble()))
            image.renderingHints?.let(g::setRenderingHints)
            image.paint(g)
        } finally {
            g.dispose()
        }
    }
}
