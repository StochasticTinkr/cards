package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.StandardDeck
import kotlin.random.Random

class SolitaireGame {
    // Indicates whether the most recent state change was due to undo/redo history navigation
    var lastChangeFromHistory: Boolean = false
        private set
    private val listeners = SolitaireListeners()
    private var random = Random(System.nanoTime())
    private var state: SolitaireState = newGameState()
        set(value) {
            val oldState = field
            field = value
            listeners.stateChanged(oldState, value)
        }
    private val undoHistory = mutableListOf<SolitaireState>()
    private val redoHistory = mutableListOf<SolitaireState>()

    val currentState: SolitaireState get() = state
    private val selectCards = mutableListOf<Card>()
    private var sourceContainer: CardSource? = null

    val hasSelection get() = selectCards.isNotEmpty() && sourceContainer != null
    val numberOfCardsInStock get() = currentState.stock.size

    fun newGame() {
        val newGameState = newGameState()
        listeners.newGame(newGameState)
        state = newGameState
        undoHistory.clear()
        redoHistory.clear()
        clearSelection()
    }

    private fun newGameState(): SolitaireState {
        val deck = StandardDeck.cards.shuffled(random).toMutableList()
        val hiddenTableau = List(7) { i ->
            val cards = mutableListOf<Card>()
            repeat(6 - i) {
                cards.add(deck.removeLast())
            }
            cards.toList()
        }
        val visibleTableau = List(7) { mutableListOf(deck.removeLast()) }
        val waste = listOf(deck.removeLast())
        val stock = deck.toList()

        return SolitaireState(mapOf(), hiddenTableau, visibleTableau, stock, waste)
    }


    fun pullFromStock() {
        pushState(currentState.pullFromStock())
    }

    private fun pushState(state: SolitaireState) {
        redoHistory.clear()
        undoHistory.add(currentState)
        lastChangeFromHistory = false
        this.state = state
    }

    fun undo() {
        undoHistory.removeLastOrNull()?.let { previousState ->
            redoHistory.add(currentState)
            lastChangeFromHistory = true
            this.state = previousState
        }
    }

    fun redo() {
        redoHistory.removeLastOrNull()?.let { nextState ->
            undoHistory.add(currentState)
            lastChangeFromHistory = true
            this.state = nextState
        }
    }

    fun moveSelectedCardsTo(target: CardReceiver): Boolean {
        if (target == sourceContainer) {
            clearSelection()
            return false
        }
        val cardsToMove = target.canReceive(selectCards, currentState)
        if (cardsToMove.isEmpty()) {
            return false
        }
        transfer(target, cardsToMove, sourceContainer!!)
        clearSelection()
        return true
    }

    fun select(from: SourcedCard) {
        val (container, card) = from
        selectCards.clear()
        val cards = container.availableFrom(card, currentState)
        selectCards.addAll(cards)
        if (selectCards.isNotEmpty()) {
            sourceContainer = container
            listeners.selectionChanged(container, cards)
        } else {
            listeners.selectionCleared()
        }
    }

    private fun clearSelection() {
        selectCards.clear()
        sourceContainer = null
        listeners.selectionCleared()
    }

    fun isSelected(card: Card) = selectCards.contains(card)

    fun canAutoFinish(): Boolean = currentState.tableauHidden.all { it.isEmpty() }

    /**
     * Perform a single auto-finish step.
     * - If a Tableau/Waste top card can move to a foundation, moves the lowest-rank candidate.
     * - Otherwise, pulls from stock to waste. Returns true if any change occurred.
     */
    fun autoFinishStep(): Boolean {
        // Find candidates from tableau (top visible of each pile)
        val state = currentState
        data class Candidate(val source: CardSource, val receivable: List<Card>, val rankOrdinal: Int)
        val candidates = mutableListOf<Candidate>()
        // Tableau candidates
        state.tableauVisible.forEachIndexed { idx, visible ->
            val top = visible.lastOrNull() ?: return@forEachIndexed
            val source = TableauSource(idx)
            val avail = source.availableFrom(top, state)
            val receivable = Foundations.canReceive(avail, state)
            if (receivable.isNotEmpty()) {
                val rankOrdinal = receivable.last().rank.ordinal
                candidates += Candidate(source, receivable, rankOrdinal)
            }
        }
        // Waste candidate
        state.waste.lastOrNull()?.let { top ->
            val source = WastePile
            val avail = source.availableFrom(top, state)
            val receivable = Foundations.canReceive(avail, state)
            if (receivable.isNotEmpty()) {
                val rankOrdinal = receivable.last().rank.ordinal
                candidates += Candidate(source, receivable, rankOrdinal)
            }
        }
        if (candidates.isNotEmpty()) {
            val chosen = candidates.minBy { it.rankOrdinal }
            transfer(Foundations, chosen.receivable, chosen.source)
            return true
        }
        // No moves possible -> try pulling from stock
        val before = currentState
        pullFromStock()
        return currentState !== before
    }

    fun autoMoveCard(from: SourcedCard): Boolean {
        val (container, card) = from
        clearSelection()
        val availableCards = container.availableFrom(card, currentState)
        if (availableCards.isEmpty()) {
            return false
        }
        val foundationsCanReceive = Foundations.canReceive(availableCards, currentState)
        if (foundationsCanReceive.isNotEmpty()) {
            transfer(Foundations, foundationsCanReceive, container)
            return true
        }
        val tableauTarget =
            List(7, ::TableauReceiver).asSequence()
                .map { it to it.canReceive(availableCards, currentState) }
                .filter { (_, receivable) -> receivable.isNotEmpty() }
                .maxByOrNull { (_, receivable) -> receivable.size }

        if (tableauTarget != null) {
            val (receiver, cards) = tableauTarget
            transfer(receiver, cards, container)
            return true
        }
        return false
    }

    private fun transfer(target: CardReceiver, cards: List<Card>, source: CardSource) {
        pushState(source.transfer(cards, target, currentState))
    }


    fun addListener(listener: SolitaireListener) {
        listeners.add(listener)
    }

    @Suppress("unused")
    fun removeListener(listener: SolitaireListener) {
        listeners.remove(listener)
    }

}
