package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile(val solitaireListener: SolitaireListener) : CardSource {
    private val cards = mutableListOf<Card>()

    fun clear() {
        cards.clear()
    }
    fun add(card: Card) {
        cards.add(card)
    }

    override fun availableFrom(card: Card): List<Card> =
        if (cards.lastOrNull() == card) listOf(card) else emptyList()

    override fun take(cards: List<Card>): List<Card> {
        require(cards.size == 1 && cards.first() == this.cards.lastOrNull())
        this.cards.removeLast()
        return cards
    }

    fun returnCardsTo(stock: StockPile) {
        val newDeck = cards.reversed()
        solitaireListener.wasteRestocked(this, newDeck, stock)
        stock.setDeck(newDeck)
        cards.clear()
    }

    fun onVisibleCard(block: (Card)->Unit) {
        cards.lastOrNull()?.let(block)
    }
}
