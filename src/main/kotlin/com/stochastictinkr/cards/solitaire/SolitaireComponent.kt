package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.skywing.awt.geom.point
import com.stochastictinkr.skywing.awt.geom.roundRectangle
import com.stochastictinkr.skywing.awt.hints
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities


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
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    println("Not left button pressed")
                    return
                }
                println("Left button pressed")
                if (e.clickCount == 2) {
                    println("Double Click")
                    withCardAt(e.point) { container, card ->
                        println("Clicked on card: ${card.name}")
                        val availableCards = container.availableFrom(card)
                        println("can move ${availableCards.size} cards")
                        if (availableCards.isNotEmpty()) {
                            sequenceOf(
                                solitaireModel.foundations,
                                solitaireModel.tableauPiles
                            )
                                .flatMap { it.asSequence() }
                                .map { it to it.canReceive(availableCards) }
                                .firstOrNull { (_, receivable) -> receivable.isNotEmpty() }
                                ?.also { (target, receivable) ->
                                    println("Moving $receivable to $target")
                                    target.receive(container.take(receivable))
                                    repaint()
                                }.also {
                                    if (it == null) {
                                        println("Nothing to move")
                                    }
                                }
                        }
                    }
                }
            }

        })
    }

    private inline fun <T> withCardAt(point: Point, function: (CardContainer, Card) -> T): T? {
        var highestFound = Int.MAX_VALUE
        var bestFound: Pair<CardContainer, Card>? = null
        visitAll(object : CardsVisitor {
            override fun otherVisibleCard(
                container: CardContainer,
                card: Card,
                position: Point,
                width: Int,
                height: Int,
            ) {
                if (Rectangle(position, Dimension(width, height)).contains(point)) {
                    if (bestFound == null || highestFound < position.y) {
                        bestFound = container to card
                        highestFound = position.y
                    }
                }
            }

            override fun otherHiddenCard(
                container: CardContainer,
                card: Card,
                position: Point,
                width: Int,
                height: Int,
            ) {
                if (Rectangle(position, Dimension(width, height)).contains(point)) {
                    if (bestFound == null || highestFound < position.y) {
                        bestFound = container to card
                        highestFound = position.y
                    }
                }
            }
        })
        return bestFound?.let { (container, card) ->
            println("found ${card.name}")
            function(container, card)
        }.also {
            if (it == null) {
                println("None found")
            }
        }
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
            override fun otherPosition(position: Point, width: Int, height: Int) =
                g.drawPlacementOutline(position, width, height)

            override fun otherVisibleCard(
                container: CardContainer,
                card: Card,
                position: Point,
                width: Int,
                height: Int,
            ) = g.drawCard(card, position)

            override fun otherHiddenCard(
                container: CardContainer,
                card: Card,
                position: Point,
                width: Int,
                height: Int,
            ) = g.drawCardBack(position)
        })
    }

    private fun Graphics2D.drawCardBack(position: Point) {
        drawImage(images[cardBack], position.x, position.y, null)
    }

    private fun Graphics2D.drawCard(card: Card, position: Point) {
        drawImage(images[card], position.x, position.y, null)
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
                cardsVisitor.hiddenTableauCard(it, position, cardWidth, cardHeight, pile, index)
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
            solitaireModel.stock.cards.forEach {
                cardsVisitor.stockCard(it, position, cardWidth, cardHeight, solitaireModel.stock)
                position.y += stockFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY + cardHeight + 10)
            cardsVisitor.wastePosition(position, cardWidth, cardHeight)
            solitaireModel.wastePile.cards.lastOrNull()?.let { card ->
                cardsVisitor.wasteCard(card, position, cardWidth, cardHeight, solitaireModel.wastePile)
            }
        }
    }

    private interface CardsVisitor {
        fun otherPosition(position: Point, width: Int, height: Int) {}
        fun otherVisibleCard(container: CardContainer, card: Card, position: Point, width: Int, height: Int) {}
        fun otherHiddenCard(container: CardContainer, card: Card, position: Point, width: Int, height: Int) {}

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
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            foundationPile: FoundationPile,
            index: Int,
        ) {
            otherVisibleCard(foundationPile, card, position, width, height)
        }

        fun visibleTableauCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherVisibleCard(tableauPile, card, position, width, height)
        }

        fun hiddenTableauCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherHiddenCard(tableauPile, card, position, width, height)
        }

        fun stockCard(card: Card, position: Point, width: Int, height: Int, stock: StockPile) {
            otherHiddenCard(stock, card, position, width, height)
        }

        fun wasteCard(card: Card, position: Point, width: Int, height: Int, wastePile: WastePile) {
            otherVisibleCard(wastePile, card, position, width, height)
        }
    }

    private fun Graphics2D.drawPlacementOutline(position: Point, cardWidth: Int, cardHeight: Int) {
        color = Color.BLACK
        draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardWidth + 4.0, cardHeight + 4.0, 4.0, 4.0)
        })
    }
}
