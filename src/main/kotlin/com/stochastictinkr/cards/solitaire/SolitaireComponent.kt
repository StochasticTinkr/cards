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
import kotlin.math.max
import kotlin.math.min


class SolitaireComponent(val solitaireGame: SolitaireGame) : JComponent() {
    var cardBack = CardBacks.BLUE
        set(value) {
            field = value;
            repaint()
        }
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

    private val wastePosition get() = point(width - cardSize.width * 2 - 8, tableauY + cardSize.height + 10)
    private val wasteRectangle get() = Rectangle(wastePosition, cardSize)

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

    private data class SourcedCard(val rectangle: Rectangle, val cardSource: CardSource, val card: Card)

    private val sourcedCards
        get() = sequence {
            val state = solitaireGame.currentState
            state.waste.lastOrNull()?.let { card ->
                yield(SourcedCard(wasteRectangle, WastePile, card))
            }
            var x = tableauX
            repeat(7) { idx ->
                val position = Rectangle(
                    x,
                    tableauY + tableauHiddenCardFanHeight * state.tableauHidden[idx].size,
                    cardSize.width,
                    cardSize.height
                )
                state.tableauVisible[idx].forEachIndexed { index, card ->
                    yield(SourcedCard(Rectangle(position), TableauSource(idx), card))
                    position.y += tableauVisibleCardFanHeight
                }
                x += tableauMargin + cardSize.width
            }
        }

    private fun <T> withSourcedCardAt(point: Point, function: (CardSource, Card) -> T): T? {
        val state = solitaireGame.currentState
        if (point in wasteRectangle) {
            return state.waste.lastOrNull()?.let { card -> function(WastePile, card) }
        }
        val index = (point.x - tableauX) / (tableauMargin + cardSize.width)
        if (index !in state.tableauVisible.indices || point.y < tableauY) {
            return null
        }

        val numHiddenCards = state.tableauHidden[index].size
        val visibleCards = state.tableauVisible[index]
        val tableauHeight = cardSize.height +
                (tableauHiddenCardFanHeight * numHiddenCards + tableauVisibleCardFanHeight * visibleCards.size)
        val offsetY = point.y - tableauY
        if (offsetY > tableauHeight) {
            return null
        }
        val cardIndex = min(
            max(
                0,
                (offsetY - (numHiddenCards * tableauHiddenCardFanHeight)) / tableauVisibleCardFanHeight
            ), visibleCards.size - 1
        )
        return function(TableauSource(index), visibleCards[cardIndex])
    }

    override fun paintComponent(g: Graphics) {
        require(g is Graphics2D)
        g.hints {
            renderingQuality()
            antialiasingOn()
        }
        g.color = Color(70, 120, 80)
        g.fillRect(0, 0, width, height)
        val state = solitaireGame.currentState

        CardSuit.values().forEachIndexed { index, suit ->
            val position = point(foundationX + (cardSize.width + foundationMargin) * index, foundationY)
            g.drawPlacementOutline(position)
            state.foundations[suit]?.let { rank ->
                g.drawCard(Card(suit, rank), position)
            }
        }
        repeat(7) { index ->
            val position = point(tableauX + (cardSize.width + tableauMargin) * index, tableauY)
            g.drawPlacementOutline(position)
            state.tableauHidden[index].forEach { _ ->
                g.drawCardBack(position)
                position.y += tableauHiddenCardFanHeight
            }
            state.tableauVisible[index].forEach { card ->
                g.drawCard(card, position)
                position.y += tableauVisibleCardFanHeight
            }
        }

        run {
            val position = stockStartPoint
            g.drawPlacementOutline(position)
            repeat(solitaireGame.currentState.stock.size) { card ->
                g.drawCardBack(position)
                position.y += stockFanHeight
            }
        }

        g.drawPlacementOutline(wastePosition)
        solitaireGame.currentState.waste.lastOrNull()?.let { card ->
            g.drawCard(card, wastePosition)
        }
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

    private val stockStartPoint get() = point(width - images.cardWidth * 2 - 8, tableauY)

    private fun Graphics2D.drawPlacementOutline(position: Point) {
        color = Color.BLACK
        draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardSize.width + 4.0, cardSize.height + 4.0, 4.0, 4.0)
        })
    }
}
