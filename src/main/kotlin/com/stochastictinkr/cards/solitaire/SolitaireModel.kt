package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.cards.standard.StandardDeck
import kotlin.random.Random

class SolitaireModel {
    private val listeners = mutableListOf<SolitaireListener>()
    private var random = Random(System.nanoTime())
    val foundations = CardSuit.values().map { FoundationPile(it, this) }
    val stock = StockPile(this)
    val tableauPiles = List(7) { TableauPile(this) }
    val wastePile = WastePile(this)

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
            for (j in tableauPiles.indices.drop(i+1)) {
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

    fun moveSelectedCardsTo(receiver: CardReceiver): Boolean {
        if (receiver == sourceContainer) {
            clearSelection()
            return false
        }
        val canReceive = receiver.canReceive(selectCards)
        if (canReceive.isEmpty()) {
            return false
        }
        receiver.receive(sourceContainer!!.take(canReceive))
        clearSelection()
        return true
    }


    fun select(container: CardSource, card: Card) {
        selectCards.clear()
        selectCards.addAll(container.availableFrom(card))
        sourceContainer = if (selectCards.isNotEmpty()) container else null
    }

    private fun clearSelection() {
        selectCards.clear()
        sourceContainer = null
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
            foundationTarget.receiveFrom(container)
            return true
        }
        val tableauTarget =
            tableauPiles.asSequence()
                .map { it to it.canReceive(availableCards) }
                .filter { (_, receivable) -> receivable.isNotEmpty() }
                .maxByOrNull { (_, receivable) -> receivable.size }
        if (tableauTarget != null) {
            tableauTarget.receiveFrom(container)
            return true
        }
        return false
    }

    private fun Pair<CardReceiver, List<Card>>.receiveFrom(container: CardSource) {
        val (receiver, cards) = this
        receiver.receive(container.take(cards))
    }
}
