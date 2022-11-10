package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.skywing.awt.geom.point
import com.stochastictinkr.skywing.awt.geom.roundRectangle
import com.stochastictinkr.skywing.awt.hints
import com.stochastictinkr.skywing.swing.action
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities


class SolitaireComponent(val solitaireGame: SolitaireGame) : JComponent() {
    private var cardBack = CardBacks.BLUE
    private val images = CardImages()
    private val foundationMargin = 15
    private val foundationX get() = width / 2 - (images.cardWidth * 2 + foundationMargin + foundationMargin / 2)
    private val foundationY = 15
    private val tableauMargin = 12
    private val tableauX = 8
    private val tableauY: Int get() = foundationY + images.cardHeight + foundationMargin + tableauMargin

    private val tableauHiddenCardFanHeight get() = images.cardHeight / 13
    private val tableauVisibleCardFanHeight get() = images.cardHeight / 6
    private val stockFanHeight get() = -images.cardHeight / 90


    private val mouseListener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return
            }
            if (e.point in stockBounds) {
                solitaireGame.pullFromStock()
                return
            }
            if (e.clickCount and 1 == 0) {
                withSourcedCardAt(e.point) { container, card ->
                    solitaireGame.autoMoveCard(container, card)
                }
                return
            }
            if (e.clickCount == 1) {
                if (solitaireGame.hasSelection) {
                    val success = withReceiverAt(e.point) { solitaireGame.moveSelectedCardsTo(it) }
                    if (success == true) {
                        return
                    }
                }
                withSourcedCardAt(e.point) { container, card ->
                    solitaireGame.select(container, card)
                }
                return
            }
        }
    }

    private val solitaireListener = object : SolitaireListener {
        override fun stateChanged(oldState: SolitaireState, newState: SolitaireState) {
            repaint()
        }

        override fun selectionChanged(cardSource: CardSource, selectedCards: List<Card>) {
            repaint()
        }

        override fun selectionCleared() {
            repaint()
        }
    }


    init {
        addMouseListener(mouseListener)
        addMouseMotionListener(mouseListener)
        addMouseWheelListener(mouseListener)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK), "Undo")
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            "Redo"
        )
        actionMap.put("Undo", action { solitaireGame.undo() })
        actionMap.put("Redo", action { solitaireGame.redo() })
        solitaireGame.addListener(solitaireListener)
    }

    private inline fun <T> withReceiverAt(point: Point, function: (CardReceiver) -> T): T? {
        var result: CardReceiver? = null
        visitAll(object : CardsVisitor() {
            override fun foundationPosition(
                position: Point,
                width: Int,
                height: Int,
                foundationPile: FoundationPile,
                index: Int,
            ) {
                if (point in Rectangle(position, Dimension(width, height))) {
                    result = foundationPile
                    done()
                }
            }

            override fun tableauPosition(
                position: Point,
                width: Int,
                height: Int,
                tableauPile: TableauPile,
                index: Int,
            ) {
                if (point in Rectangle(
                        position,
                        Dimension(
                            width,
                            height + tableauPile.hiddenCardCount * tableauHiddenCardFanHeight + tableauPile.visibleCardCount * tableauVisibleCardFanHeight
                        )
                    )
                ) {
                    result = tableauPile
                    done()
                }
            }
        })
        return result?.let(function)
    }

    private inline fun <T> withSourcedCardAt(point: Point, function: (CardSource, Card) -> T): T? {
        var highestFound = Int.MAX_VALUE
        var bestFound: Pair<CardSource, Card>? = null
        visitAll(object : CardsVisitor() {
            override fun visibleTableauCard(
                card: Card,
                position: Point,
                width: Int,
                height: Int,
                tableauPile: TableauPile,
                index: Int,
            ) {
                if (Rectangle(position, Dimension(width, height)).contains(point)) {
                    if (bestFound == null || highestFound < position.y) {
                        bestFound = tableauPile to card
                        highestFound = position.y
                    }
                }
            }

            override fun wasteCard(card: Card, position: Point, width: Int, height: Int, wastePile: WastePile) {
                if (Rectangle(position, Dimension(width, height)).contains(point)) {
                    if (bestFound == null || highestFound < position.y) {
                        bestFound = wastePile to card
                        highestFound = position.y
                    }
                }
            }
        })

        return bestFound?.let { (container, card) ->
            function(container, card)
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

        visitAll(object : CardsVisitor() {
            override fun otherPosition(container: CardLocation, position: Point, width: Int, height: Int) =
                g.drawPlacementOutline(position, width, height)

            override fun otherVisibleCard(
                container: CardLocation,
                card: Card,
                position: Point,
                width: Int,
                height: Int,
            ) = g.drawCard(card, position)

            override fun otherHiddenCard(
                container: CardLocation,
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
        val image = images[card]
        if (solitaireGame.isSelected(card)) {
            paint = Color.YELLOW
            fillRoundRect(position.x - 1, position.y - 1, image.width + 2, image.height + 2, 5, 5)
        }
        drawImage(image, position.x, position.y, null)
    }

    private val stockBounds: Rectangle
        get() {
            val movement = stockFanHeight * solitaireGame.numberOfCardsInStock
            val startPoint = stockStartPoint
            return Rectangle(startPoint.x, startPoint.y + movement, images.cardWidth, images.cardHeight - movement)
        }


    private fun visitAll(cardsVisitor: CardsVisitor) {
        images.cardWidth = width / 10
        val cardWidth = images.cardWidth
        val cardHeight = images.cardHeight
        solitaireGame.foundations.forEachIndexed { index, pile ->
            val position = point(foundationX + (cardWidth + foundationMargin) * index, foundationY)
            cardsVisitor.foundationPosition(position, cardWidth, cardHeight, pile, index)
            pile.onVisibleCard { visibleCard ->
                cardsVisitor.foundationCard(visibleCard, position, cardWidth, cardHeight, pile, index)
            }
        }

        solitaireGame.tableauPiles.forEachIndexed { index, pile ->
            val position = point(tableauX + (cardWidth + tableauMargin) * index, tableauY)
            cardsVisitor.tableauPosition(position, cardWidth, cardHeight, pile, index)
            pile.forEachHiddenCard {
                cardsVisitor.hiddenTableauCard(it, position, cardWidth, cardHeight, pile, index)
                position.y += tableauHiddenCardFanHeight
            }
            pile.forEachVisibleCard { card ->
                cardsVisitor.visibleTableauCard(card, position, cardWidth, cardHeight, pile, index)
                position.y += tableauVisibleCardFanHeight
            }
        }

        run {
            val position = stockStartPoint
            cardsVisitor.stockPosition(position, cardWidth, cardHeight, StockPile)
            solitaireGame.currentState.stock.forEach { card ->
                cardsVisitor.stockCard(card, position, cardWidth, cardHeight, StockPile)
                position.y += stockFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY + cardHeight + 10)
            cardsVisitor.wastePosition(position, cardWidth, cardHeight, solitaireGame.wastePile)
            solitaireGame.wastePile.onVisibleCard { card ->
                cardsVisitor.wasteCard(card, position, cardWidth, cardHeight, solitaireGame.wastePile)
            }
        }
    }

    private val stockStartPoint get() = point(width - images.cardWidth * 2 - 8, tableauY)

    private abstract class CardsVisitor {
        var shouldContinue = true

        open fun otherPosition(container: CardLocation, position: Point, width: Int, height: Int) {}
        open fun otherVisibleCard(
            container: CardLocation,
            card: Card,
            position: Point,
            width: Int,
            height: Int,
        ) {
        }

        open fun otherHiddenCard(
            container: CardLocation,
            card: Card,
            position: Point,
            width: Int,
            height: Int,
        ) {
        }

        open fun foundationPosition(
            position: Point,
            width: Int,
            height: Int,
            foundationPile: FoundationPile,
            index: Int,
        ) {
            otherPosition(foundationPile, position, width, height)
        }

        open fun tableauPosition(
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherPosition(tableauPile, position, width, height)
        }

        open fun stockPosition(position: Point, width: Int, height: Int, stockPile: StockPile) {
            otherPosition(stockPile, position, width, height)
        }

        open fun wastePosition(position: Point, width: Int, height: Int, wastePile: WastePile) {
            otherPosition(wastePile, position, width, height)
        }

        open fun foundationCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            foundationPile: FoundationPile,
            index: Int,
        ) {
            otherVisibleCard(foundationPile, card, position, width, height)
        }

        open fun visibleTableauCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherVisibleCard(tableauPile, card, position, width, height)
        }

        open fun hiddenTableauCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherHiddenCard(tableauPile, card, position, width, height)
        }

        open fun stockCard(card: Card, position: Point, width: Int, height: Int, stock: StockPile) {
            otherHiddenCard(stock, card, position, width, height)
        }

        open fun wasteCard(card: Card, position: Point, width: Int, height: Int, wastePile: WastePile) {
            otherVisibleCard(wastePile, card, position, width, height)
        }

        fun done() {
            shouldContinue = false
        }
    }

    private fun Graphics2D.drawPlacementOutline(position: Point, cardWidth: Int, cardHeight: Int) {
        color = Color.BLACK
        draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardWidth + 4.0, cardHeight + 4.0, 4.0, 4.0)
        })
    }
}
