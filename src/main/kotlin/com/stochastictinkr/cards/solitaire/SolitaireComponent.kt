package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.cards.CardImages
import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.cards.standard.StandardDeck
import com.stochastictinkr.skywing.geom.point
import com.stochastictinkr.skywing.geom.roundRectangle
import com.stochastictinkr.skywing.rendering.hints
import com.stochastictinkr.skywing.geom.by
import com.stochastictinkr.skywing.geom.component1
import com.stochastictinkr.skywing.geom.component2
import com.stochastictinkr.utils.isEven
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.prefs.Preferences
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.max
import kotlin.math.min


class SolitaireComponent(val solitaireGame: SolitaireGame) : JComponent() {
    private val preferences get() = Preferences.userNodeForPackage(javaClass)!!

    var cardBack = CardBacks.valueOf(preferences["Back", CardBacks.BLUE.name])
        set(value) {
            field = value
            preferences.put("Back", value.name)
            repaint()
        }

    var animationsEnabled: Boolean = preferences.getBoolean("Animations", true)
        set(value) {
            field = value
            preferences.putBoolean("Animations", value)
        }

    var autoFinishEnabled: Boolean = preferences.getBoolean("AutoFinish", false)
        set(value) {
            field = value
            preferences.putBoolean("AutoFinish", value)
        }

    private val images = CardImages()
    private val cardSize get() = images.cardWidth by images.cardHeight
    private val numFoundations = CardSuit.entries.size

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
    private val displayModel = CardDisplayModel(solitaireGame::isSelected, images, getBack = { cardBack }, animationsEnabled = { animationsEnabled })

    // Listeners
    private var autoFinishTimer: Timer? = null
    private var performingAutoFinish: Boolean = false

    private val mouseListener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return
            }
            // Any user interaction interrupts auto-finish animations
            autoFinishTimer?.stop()
            autoFinishTimer = null
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
        override fun newGame(state: SolitaireState) {
            displayModel.clear()
            StandardDeck.cards.forEach {
                displayModel[it] = CardDisplayModel.State(stockStartPoint, 0f, 0f)
            }
            repaint()
        }

        override fun stateChanged(oldState: SolitaireState, newState: SolitaireState) {
            updateDisplay()
            repaint()
            maybeStartAutoFinish()
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
                updateDisplay()
                repaint()
            }
        })
        addMouseListener(mouseListener)
        addMouseMotionListener(mouseListener)
        addMouseWheelListener(mouseListener)

        solitaireGame.addListener(solitaireListener)
        updateDisplay()
        Timer(125) {
            if (displayModel.needsUpdate()) {
                repaint()
            }
        }.apply {
            isRepeats = true
            delay = 13
            start()
        }
    }

    private fun updateDisplay() {
        CardSuit.entries.forEachIndexed { index, suit ->
            val position = point(foundationX + (cardSize.width + foundationMargin) * index, foundationY)
            solitaireGame.currentState.foundations[suit]?.let { rank ->
                displayModel[Card(suit, rank)].setTarget(position, true, 100)
                rank.previous?.let { previous ->
                    displayModel[Card(suit, previous)].setTarget(position, true, 99)
                }
            }
        }
        solitaireGame.currentState.waste.forEachIndexed { index, card ->
            displayModel[card].setTarget(wastePosition, true, index)
        }
        solitaireGame.currentState.stock.forEachIndexed { index, card ->
            displayModel[card].setTarget(stockStartPoint.apply {
                y += index * stockFanHeight
            }, false, index)
        }
        val hiddenCards = solitaireGame.currentState.tableauHidden
        val visibleCards = solitaireGame.currentState.tableauVisible
        repeat(7) { tableauNumber ->
            hiddenCards[tableauNumber].forEachIndexed { index, card ->
                val position = point(
                    tableauX + (cardSize.width + tableauMargin) * tableauNumber,
                    tableauY + index * tableauHiddenCardFanHeight
                )
                displayModel[card].setTarget(position, false, index - 50)
            }
            val startY = tableauY + hiddenCards[tableauNumber].size * tableauHiddenCardFanHeight
            visibleCards[tableauNumber].forEachIndexed { index, card ->
                val position = point(
                    tableauX + (cardSize.width + tableauMargin) * tableauNumber,
                    startY + index * tableauVisibleCardFanHeight
                )
                displayModel[card].setTarget(position, true, index)
            }
        }

    }

    private fun shouldAutoFinish(): Boolean {
        return autoFinishEnabled && solitaireGame.canAutoFinish() && !solitaireGame.lastChangeFromHistory
    }

    private fun maybeStartAutoFinish() {
        if (!shouldAutoFinish()) return
        if (performingAutoFinish) return
        if (animationsEnabled) {
            startAutoFinishTimer()
        } else {
            // Non-animated: complete immediately
            performingAutoFinish = true
            try {
                // Keep stepping until blocked or conditions change
                var progressed = true
                while (progressed && shouldAutoFinish()) {
                    progressed = solitaireGame.autoFinishStep()
                }
            } finally {
                performingAutoFinish = false
            }
        }
    }

    private fun startAutoFinishTimer() {
        // If a timer is already running, let it continue
        if (autoFinishTimer != null) return
        performingAutoFinish = true
        autoFinishTimer = Timer(125) {
            if (!shouldAutoFinish()) {
                autoFinishTimer?.stop()
                autoFinishTimer = null
                performingAutoFinish = false
                return@Timer
            }
            val progressed = solitaireGame.autoFinishStep()
            if (!progressed) {
                autoFinishTimer?.stop()
                autoFinishTimer = null
                performingAutoFinish = false
            }
        }.apply {
            isRepeats = true
            start()
        }
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

        displayModel.drawAll(g)
    }

    private fun Graphics2D.paintWaste() {
        drawPlacementOutline(wastePosition)
    }

    private fun Graphics2D.paintStock() {
        drawPlacementOutline(stockStartPoint)
    }

    private fun Graphics2D.paintTableau() {
        repeat(7) { tableauNumber ->
            drawPlacementOutline(point(tableauX + (cardSize.width + tableauMargin) * tableauNumber, tableauY))
        }
    }

    private fun Graphics2D.paintFoundation() {
        CardSuit.entries.indices.forEach { index ->
            drawPlacementOutline(point(foundationX + (cardSize.width + foundationMargin) * index, foundationY))
        }
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

        if (visibleCards.isEmpty()) {
            return null
        }

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
