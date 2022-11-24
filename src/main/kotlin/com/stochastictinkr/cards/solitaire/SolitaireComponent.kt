package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.skywing.awt.geom.point
import com.stochastictinkr.skywing.awt.geom.roundRectangle
import com.stochastictinkr.skywing.awt.hints
import com.stochastictinkr.skywing.rendering.geom.by
import com.stochastictinkr.skywing.rendering.geom.component1
import com.stochastictinkr.skywing.rendering.geom.component2
import com.stochastictinkr.skywing.swing.action
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
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

    private val cardSize get() = images.cardWidth by images.cardHeight

    private val numFoundations = CardSuit.values().size

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
                    val success = withReceiverAt(e.point) {
                        solitaireGame.moveSelectedCardsTo(it)
                    }
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
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                images.cardWidth = width / 10
                repaint()
            }
        })
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

    private data class Receiver(val rectangle: Rectangle, val cardReceiver: CardReceiver)

    private val foundationRectangle
        get() =
            Rectangle(
                point(foundationX, foundationY),
                (cardSize.width * numFoundations + foundationMargin * numFoundations) by cardSize.height
            )

    private val tableauRectangles
        get() = sequence {
            val (cardWidth, cardHeight) = cardSize
            repeat(7) { index ->
                val state = solitaireGame.currentState
                val tableauHeight = (tableauHiddenCardFanHeight * state.tableauHidden[index].size +
                        tableauVisibleCardFanHeight * state.tableauVisible[index].size
                        + cardHeight)
                yield(
                    Rectangle(
                        point(tableauX + (cardWidth + tableauMargin) * index, tableauY),
                        cardWidth by tableauHeight
                    )
                )
            }
        }

    private val receivers = sequence {
        yield(Receiver(foundationRectangle, Foundations))
        yieldAll(
            tableauRectangles.mapIndexed { index, rectangle -> Receiver(rectangle, TableauReceiver(index)) }
        )
    }

    private fun <T> withReceiverAt(point: Point, function: (CardReceiver) -> T): T? {
        return receivers
            .filter { point in it.rectangle }
            .map { it.cardReceiver }
            .take(1)
            .map(function)
            .firstOrNull()
    }

    private data class SourcedCard(val rectangle: Rectangle, val cardReceiver: CardSource, val card: Card)

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

            override fun wasteCard(card: Card, position: Point, width: Int, height: Int) {
                if (Rectangle(position, Dimension(width, height)).contains(point)) {
                    if (bestFound == null || highestFound < position.y) {
                        bestFound = WastePile to card
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
            override fun otherPosition(position: Point, width: Int, height: Int) =
                g.drawPlacementOutline(position, width, height)

            override fun otherVisibleCard(
                card: Card,
                position: Point,
                width: Int,
                height: Int,
            ) = g.drawCard(card, position)

            override fun otherHiddenCard(
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
        val (cardWidth, cardHeight) = cardSize
        CardSuit.values().forEachIndexed { index, suit ->
            val position = point(foundationX + (cardWidth + foundationMargin) * index, foundationY)
            cardsVisitor.foundationPosition(position, cardWidth, cardHeight, index)
            solitaireGame.currentState.foundations[suit]?.let { rank ->
                cardsVisitor.foundationCard(Card(suit, rank), position, cardWidth, cardHeight, index)

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
            cardsVisitor.stockPosition(position, cardWidth, cardHeight)
            solitaireGame.currentState.stock.forEach { card ->
                cardsVisitor.stockCard(card, position, cardWidth, cardHeight)
                position.y += stockFanHeight
            }
        }

        run {
            val position = point(width - cardWidth * 2 - 8, tableauY + cardHeight + 10)
            cardsVisitor.wastePosition(position, cardWidth, cardHeight)
            solitaireGame.currentState.waste.lastOrNull()?.let { card ->
                cardsVisitor.wasteCard(card, position, cardWidth, cardHeight)
            }
        }
    }

    private val stockStartPoint get() = point(width - images.cardWidth * 2 - 8, tableauY)

    private abstract class CardsVisitor {
        open fun otherPosition(position: Point, width: Int, height: Int) {}
        open fun otherVisibleCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
        ) {
        }

        open fun otherHiddenCard(
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
            index: Int,
        ) {
            otherPosition(position, width, height)
        }

        open fun tableauPosition(
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherPosition(position, width, height)
        }

        open fun stockPosition(position: Point, width: Int, height: Int) {
            otherPosition(position, width, height)
        }

        open fun wastePosition(position: Point, width: Int, height: Int) {
            otherPosition(position, width, height)
        }

        open fun foundationCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            index: Int,
        ) {
            otherVisibleCard(card, position, width, height)
        }

        open fun visibleTableauCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherVisibleCard(card, position, width, height)
        }

        open fun hiddenTableauCard(
            card: Card,
            position: Point,
            width: Int,
            height: Int,
            tableauPile: TableauPile,
            index: Int,
        ) {
            otherHiddenCard(card, position, width, height)
        }

        open fun stockCard(card: Card, position: Point, width: Int, height: Int) {
            otherHiddenCard(card, position, width, height)
        }

        open fun wasteCard(card: Card, position: Point, width: Int, height: Int) {
            otherVisibleCard(card, position, width, height)
        }
    }

    private fun Graphics2D.drawPlacementOutline(position: Point, cardWidth: Int, cardHeight: Int) {
        color = Color.BLACK
        draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardWidth + 4.0, cardHeight + 4.0, 4.0, 4.0)
        })
    }
}
