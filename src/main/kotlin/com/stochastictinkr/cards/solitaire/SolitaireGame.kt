package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.cards.standard.StandardDeck
import kotlin.random.Random

class SolitaireGame {
    private val listeners = SolitaireListeners()
    private var random = Random(System.nanoTime())
    var state: SolitaireState = newGameState()
        set(value) {
            val oldState = field
            field = value
            listeners.stateChanged(oldState, value)
        }

    val foundations = CardSuit.values().map { suit -> FoundationPile(suit, this) }
    val tableauPiles = List(7) { idx -> TableauPile(listeners, this, idx) }
    val wastePile = WastePile(this)

    private val selectCards = mutableListOf<Card>()
    private var sourceContainer: CardSource? = null

    val hasSelection get() = selectCards.isNotEmpty() && sourceContainer != null
    val numberOfCardsInStock get() = state.stock.size

    fun newGame() {
        state = newGameState()
        clearSelection()
    }

    private fun newGameState(): SolitaireState {
        val deck = StandardDeck.cards.shuffled(random).toMutableList()
        val foundations = List(4) { emptyList<Card>() }
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

        return SolitaireState(foundations, hiddenTableau, visibleTableau, stock, waste)
    }


    fun pullFromStock() {
        state = state.pullFromStock()
    }

    fun moveSelectedCardsTo(target: CardReceiver): Boolean {
        if (target == sourceContainer) {
            clearSelection()
            return false
        }
        val cardsToMove = target.canReceive(selectCards)
        if (cardsToMove.isEmpty()) {
            return false
        }
        transfer(target, cardsToMove, sourceContainer!!)
        clearSelection()
        return true
    }


    fun select(container: CardSource, card: Card) {
        selectCards.clear()
        val cards = container.availableFrom(card)
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

    fun autoMoveCard(
        container: CardSource,
        card: Card,
    ): Boolean {
        clearSelection()
        val availableCards = container.availableFrom(card)
        if (availableCards.isEmpty()) {
            return false
        }
        val foundationTarget = foundations.map { it to it.canReceive(availableCards) }
            .firstOrNull { (_, receivable) -> receivable.isNotEmpty() }
        if (foundationTarget != null) {
            val (receiver, cards) = foundationTarget
            transfer(receiver, cards, container)
            return true
        }
        val tableauTarget =
            tableauPiles.asSequence()
                .map { it to it.canReceive(availableCards) }
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
        state = source.transfer(cards, target, state)
    }


    fun addListener(listener: SolitaireListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SolitaireListener) {
        listeners.remove(listener)
    }

}
