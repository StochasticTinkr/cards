package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.skywing.awt.geom.point
import com.stochastictinkr.skywing.awt.geom.roundRectangle
import com.stochastictinkr.skywing.awt.hints
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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

    init {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (solitaireModel.selectedCards.isEmpty()) {
                    selectCardAt(e.point)
                } else {
                    dropCardAt(e.point)
                }
            }
        })
    }

    private fun selectCardAt(point: Point) {
    }

    private fun dropCardAt(point: Point) {
    }

    override fun paintComponent(g: Graphics) {
        require(g is Graphics2D)
        g.hints {
            renderingQuality()
            antialiasingOn()
        }
        g.color = Color(70, 120, 80)
        g.fillRect(0, 0, width, height)

        visitAll(object : CardsVisitor {
            override fun otherPosition(position: Point, width: Int, height: Int) {
                drawPlacementOutline(g, position, width, height)

            }

            override fun otherVisibleCard(card: CardModel, position: Point, width: Int, height: Int) {
                g.drawImage(images[card.card], position.x, position.y, null)

            }

            override fun otherHiddenCard(position: Point, width: Int, height: Int) {
                g.drawImage(images[cardBack], position.x, position.y, null)
            }
        })
    }

    private fun visitAll(cardsVisitor: CardsVisitor) {
        images.cardWidth = width / 11
        val cardWidth = images.cardWidth
        val cardHeight = images.cardHeight
        solitaireModel.foundations.forEachIndexed { index, pile ->
            val position = point(15 + (cardWidth + foundationMargin) * index, foundationY)
            cardsVisitor.foundationPosition(position, cardWidth, cardHeight, pile, index)
            pile.cards.lastOrNull()?.let { visibleCard ->
                cardsVisitor.foundationCard(visibleCard, position, cardWidth, cardHeight, pile, index)
            }
        }

        solitaireModel.tableauPiles.forEachIndexed { index, pile ->
            val position = point(8 + (cardWidth + tableauMargin) * index, tableauY)
            cardsVisitor.tableauPosition(position, cardWidth, cardHeight, pile, index)
            pile.hiddenCards.forEach {
                cardsVisitor.hiddenTableauCard(position, cardWidth, cardHeight, pile, index)
                position.y += tableauHiddenCardFanHeight
            }
            pile.visibleCards.forEach { card ->
                cardsVisitor.visibleTableauCard(card, position, cardWidth, cardHeight, pile, index)
                position.y += tableauVisibleCardFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY)
            cardsVisitor.stockPosition(position, cardWidth, cardHeight)
            repeat(solitaireModel.stock.cards.size) {
                cardsVisitor.stockCard(position, cardWidth, cardHeight)
                position.y += stockFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY + cardHeight + 10)
            cardsVisitor.wastePosition(position, cardWidth, cardHeight)
            solitaireModel.wastePile.cards.lastOrNull()?.let { card ->
                cardsVisitor.wasteCard(card, position, cardWidth, cardHeight)
            }
        }
    }

    private interface CardsVisitor {
        fun otherPosition(position: Point, width: Int, height: Int) {}
        fun otherVisibleCard(card: CardModel, position: Point, width: Int, height: Int) {}
        fun otherHiddenCard(position: Point, width: Int, height: Int) {}

        fun foundationPosition(position: Point, width: Int, height: Int, foundationPile: FoundationPile, index: Int) {
            otherPosition(position, width, height)
        }

        fun tableauPosition(position: Point, width: Int, height: Int, tableauPile: TableauPile, index: Int) {
            otherPosition(position, width, height)
        }

        fun stockPosition(position: Point, width: Int, height: Int) {
            otherPosition(position, width, height)
        }

        fun wastePosition(position: Point, width: Int, height: Int) {
            otherPosition(position, width, height)
        }

        fun foundationCard(
            card: CardModel,
            position: Point,
            width: Int,
            height: Int,
            foundationPile: FoundationPile,
            index: Int,
        ) {
            otherVisibleCard(card, position, width, height)
        }

        fun visibleTableauCard(
            card: CardModel,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherVisibleCard(card, position, width, height)
        }

        fun hiddenTableauCard(
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherHiddenCard(position, width, height)
        }

        fun stockCard(position: Point, width: Int, height: Int) {
            otherHiddenCard(position, width, height)
        }

        fun wasteCard(card: CardModel, position: Point, width: Int, height: Int) {
            otherVisibleCard(card, position, width, height)
        }
    }

    private fun drawPlacementOutline(g: Graphics2D, position: Point, cardWidth: Int, cardHeight: Int) {
        g.color = Color.BLACK
        g.draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardWidth + 4.0, cardHeight + 4.0, 4.0, 4.0)
        })
    }
}
