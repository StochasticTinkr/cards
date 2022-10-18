package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.cards.standard.StandardDeck
import kotlin.random.Random

class SolitaireModel() {
    var random = Random(System.nanoTime())
    var isGameActive = false
    val foundations = CardSuit.values().map { FoundationPile(it) }
    val stock = StockPile()
    val tableauPiles = List(7) { TableauPile() }
    val wastePile = WastePile()

    val selectCards = mutableListOf<Card>()
    var sourceContainer: CardContainer? = null
    val hasSelection get() = selectCards.isNotEmpty() && sourceContainer != null

    fun newGame() {
        foundations.forEach { it.clear() }
        tableauPiles.forEach { it.clear() }
        stock.cards.addAll(StandardDeck.cards.shuffled(random))

        for (i in 0 until 7) {
            tableauPiles[6 - i].addVisibleCard(stock.removeTop())
            for (j in (i + 1) until 7) {
                tableauPiles[6 - j].addHiddenCard(stock.removeTop())
            }
        }
        wastePile.add(stock.removeTop())
        isGameActive = true
    }

    fun pullFromStock() {
        if (
            stock.cards.isEmpty()) {
            stock.cards.addAll(wastePile.cards.asReversed())
            wastePile.cards.clear()
        }
        stock.cards.removeLastOrNull()?.let { wastePile.add(it) }
    }

    fun moveSelectedCardsTo(container: CardContainer): Boolean {
        val canReceive = container.canReceive(selectCards)
        if (canReceive.isEmpty()) {
            return false
        }
        placeSelected(container, canReceive)
        return true
    }


    fun select(container: CardContainer, card: Card) {
        selectCards.clear()
        selectCards.addAll(container.availableFrom(card))
        sourceContainer = container
    }

    private fun placeSelected(container: CardContainer, canReceive: List<Card>) {
        selectCards.clear()
        container.receive(sourceContainer!!.take(canReceive))
        sourceContainer = null
    }

    fun isSelected(card: Card) = selectCards.contains(card)

    fun autoMoveCard(
        container: CardContainer,
        card: Card,
    ): Boolean {
        val availableCards = container.availableFrom(card)
        return if (availableCards.isNotEmpty()) {
            sequenceOf(foundations, tableauPiles)
                .flatMap { it.asSequence() }
                .map { it to it.canReceive(availableCards) }
                .firstOrNull { (_, receivable) -> receivable.isNotEmpty() }
                ?.let { (target, receivable) ->
                    target.receive(container.take(receivable))
                    true
                } == true
        } else {
            false
        }
    }


}
