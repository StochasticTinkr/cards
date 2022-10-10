package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.skywing.awt.geom.point
import com.stochastictinkr.skywing.awt.geom.roundRectangle
import com.stochastictinkr.skywing.awt.hints
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import javax.swing.JComponent


class SolitaireComponent(val solitaireModel: SolitaireModel) : JComponent() {
    var cardBack = CardBacks.BLUE
    val images = CardImages()
    val foundationMargin = 15
    val tableauMargin = 12
    val foundationY = 15
    val tableauY: Int get() = foundationY + images.cardHeight + foundationMargin + tableauMargin
    val tableauHiddenCardFanHeight get() = images.cardHeight / 8
    val tableauVisibleCardFanHeight get() = images.cardHeight / 15
    val stockFanHeight get() = -images.cardHeight / 90

    override fun paintComponent(g: Graphics) {
        images.cardWidth = width / 11
        val cardWidth = images.cardWidth
        val cardHeight = images.cardHeight
        require(g is Graphics2D)
        g.hints {
            renderingQuality()
            antialiasingOn()
        }
        g.color = Color(70, 120, 80)
        g.fillRect(0, 0, width, height)
        solitaireModel.foundations.forEachIndexed { index, pile ->
            val position = point(15 + (cardWidth + foundationMargin) * index, foundationY)
            drawPlacementOutline(g, position, cardWidth, cardHeight)
            pile.cards.lastOrNull()?.let { visibleCard ->
                g.drawImage(images[visibleCard], position.x, position.y, null)
            }
        }

        solitaireModel.tableauPiles.forEachIndexed { index, pile ->
            val position = point(8 + (cardWidth + tableauMargin) * index, tableauY)
            drawPlacementOutline(g, position, cardWidth, cardHeight)
            pile.hiddenCards.forEach {
                g.drawImage(images[cardBack], position.x, position.y, null)
                position.y += tableauHiddenCardFanHeight
            }
            pile.visibleCards.forEach {
                g.drawImage(images[it], position.x, position.y, null)
                position.y += tableauVisibleCardFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY)
            drawPlacementOutline(g, position, cardWidth, cardHeight)
            repeat(solitaireModel.stock.cards.size) {
                g.drawImage(images[cardBack], position.x, position.y, null)
                position.y += stockFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY + cardHeight + 10)
            drawPlacementOutline(g, position, cardWidth, cardHeight)
            solitaireModel.wastePile.cards.lastOrNull()?.let {
                g.drawImage(images[it], position.x, position.y, null)
            }
        }
    }

    private fun drawPlacementOutline(g: Graphics2D, position: Point, cardWidth: Int, cardHeight: Int) {
        g.color = Color.BLACK
        g.draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardWidth + 4.0, cardHeight + 4.0, 4.0, 4.0)
        })
    }
}
