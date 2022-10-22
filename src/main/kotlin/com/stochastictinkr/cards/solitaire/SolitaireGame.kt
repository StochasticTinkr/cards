package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.cards.standard.StandardDeck
import kotlin.random.Random

class SolitaireGame {
    private val listeners = SolitaireListeners()
    private var random = Random(System.nanoTime())
    val foundations = CardSuit.values().map { FoundationPile(it) }
    val stock = StockPile(listeners)
    val tableauPiles = List(7) { TableauPile(listeners) }
    val wastePile = WastePile(listeners)

    private val selectCards = mutableListOf<Card>()
    private var sourceContainer: CardSource? = null

    val hasSelection get() = selectCards.isNotEmpty() && sourceContainer != null
    val numberOfCardsInStock get() = stock.numberOfCards

    fun newGame() {
        stock.setDeck(StandardDeck.cards.shuffled(random))
        foundations.forEach { it.clear() }
        tableauPiles.forEach { it.clear() }
        wastePile.clear()

        for (i in tableauPiles.indices) {
            stock.dealFaceUpTo(tableauPiles[6 - i])
            for (j in tableauPiles.indices.drop(i + 1)) {
                stock.dealFaceDownTo(tableauPiles[6 - j])
            }
        }
        stock.dealFaceUpTo(wastePile)
        clearSelection()
    }


    fun pullFromStock() {
        if (stock.isEmpty) {
            wastePile.returnCardsTo(stock)
        }
        stock.dealFaceUpTo(wastePile)
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
        val source = sourceContainer!!
        listeners.cardsMoved(source, cardsToMove, target)
        target.receive(source.take(cardsToMove))
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
        listeners.cardsMoved(source, cards, target)
        target.receive(source.take(cards))
    }


    fun addListener(listener: SolitaireListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: SolitaireListener) {
        listeners.remove(listener)
    }

}
