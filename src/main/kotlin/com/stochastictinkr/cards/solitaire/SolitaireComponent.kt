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
import com.stochastictinkr.utils.isEven
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
            field = value
            repaint()
        }

    private val images = CardImages()
    private val cardSize get() = images.cardWidth by images.cardHeight
    private val numFoundations = CardSuit.values().size

    // Layout
    private val foundationMargin = 15
    private val foundationX get() = width / 2 - (images.cardWidth * 2 + foundationMargin + foundationMargin / 2)
    private val foundationY = 15

    private val tableauMargin = 12
    private val tableauX = 8
    private val tableauY get() = foundationY + images.cardHeight + foundationMargin + tableauMargin
    private val tableauHiddenCardFanHeight get() = images.cardHeight / 13
    private val tableauVisibleCardFanHeight get() = images.cardHeight / 6

    private val stockStartPoint get() = point(width - images.cardWidth * 2 - 8, tableauY)
    private val stockFanHeight get() = -images.cardHeight / 90

    private val wastePosition get() = point(width - cardSize.width * 2 - 8, tableauY + cardSize.height + 10)

    // Listeners
    private val mouseListener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return
            }
            val point: Point = e.point
            if (isInStock(point)) {
                solitaireGame.pullFromStock()
                return
            }
            if (e.clickCount.isEven) {
                findCardAt(point)?.let(solitaireGame::autoMoveCard)
                return
            }
            if (e.clickCount == 1) {
                if (solitaireGame.hasSelection) {
                    val success = findReceiverAt(point)?.let(solitaireGame::moveSelectedCardsTo)
                    if (success == true) {
                        return
                    }
                }
                findCardAt(point)?.let(solitaireGame::select)
                return
            }
        }

        private fun isInStock(point: Point): Boolean {
            return point in Rectangle(stockStartPoint, cardSize).apply {
                y += stockFanHeight * solitaireGame.numberOfCardsInStock
                height -= stockFanHeight * solitaireGame.numberOfCardsInStock
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

    // Set up the listeners.
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

    // Painting functions
    override fun paintComponent(g: Graphics) {
        require(g is Graphics2D)
        g.hints {
            renderingQuality()
            antialiasingOn()
        }
        g.color = Color(70, 120, 80)
        g.fillRect(0, 0, width, height)

        g.paintFoundation()
        g.paintTableau()
        g.paintStock()
        g.paintWaste()
    }

    private fun Graphics2D.paintWaste() {
        drawPlacementOutline(wastePosition)
        solitaireGame.currentState.waste.lastOrNull()?.let { card ->
            drawCard(card, wastePosition)
        }
    }

    private fun Graphics2D.paintStock() {
        drawPlacementOutline(stockStartPoint)
        solitaireGame.currentState.stock.indices.map { it * stockFanHeight }.forEach { offset ->
            drawCardBack(stockStartPoint.apply {
                y += offset
            })
        }
    }

    private fun Graphics2D.paintTableau() {
        val hiddenCards = solitaireGame.currentState.tableauHidden
        val visibleCards = solitaireGame.currentState.tableauVisible
        repeat(7) { tableauNumber ->
            val position = point(tableauX + (cardSize.width + tableauMargin) * tableauNumber, tableauY)
            drawPlacementOutline(position)
            repeat(hiddenCards[tableauNumber].size) {
                drawCardBack(position)
                position.y += tableauHiddenCardFanHeight
            }
            visibleCards[tableauNumber].forEach { card ->
                drawCard(card, position)
                position.y += tableauVisibleCardFanHeight
            }
        }
    }

    private fun Graphics2D.paintFoundation() {
        CardSuit.values().forEachIndexed { index, suit ->
            val position = point(foundationX + (cardSize.width + foundationMargin) * index, foundationY)
            drawPlacementOutline(position)
            solitaireGame.currentState.foundations[suit]?.let { rank ->
                drawCard(Card(suit, rank), position)
            }
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

    private fun Graphics2D.drawPlacementOutline(position: Point) {
        color = Color.BLACK
        draw(roundRectangle {
            setRoundRect(position.x - 2.0, position.y - 2.0, cardSize.width + 4.0, cardSize.height + 4.0, 4.0, 4.0)
        })
    }

    /**
     * @return The card receiver at the given point, or null if the point isn't on a card receiver
     */
    private fun findReceiverAt(point: Point): CardReceiver? {
        val foundationRectangle = Rectangle(
            foundationX, foundationY,
            (cardSize.width * numFoundations + foundationMargin * numFoundations), cardSize.height
        )

        if (point in foundationRectangle) {
            return Foundations
        }
        val (cardWidth, cardHeight) = cardSize
        val state = solitaireGame.currentState
        repeat(7) { index ->
            val tableauHeight = (tableauHiddenCardFanHeight * state.tableauHidden[index].size +
                    tableauVisibleCardFanHeight * state.tableauVisible[index].size
                    + cardHeight)
            val tableauRectangle =
                Rectangle(
                    tableauX + (cardWidth + tableauMargin) * index, tableauY,
                    cardWidth, tableauHeight
                )
            if (point in tableauRectangle) {
                return TableauReceiver(index)
            }
        }
        return null
    }

    /**
     * @return the SourcedCard at the given point, or null if there is none.
     */
    private fun findCardAt(point: Point): SourcedCard? {
        val state = solitaireGame.currentState
        if (point in Rectangle(wastePosition, cardSize)) {
            return state.waste.lastOrNull()?.let { SourcedCard(WastePile, it) }
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
        return SourcedCard(TableauSource(index), visibleCards[cardIndex])
    }
}
