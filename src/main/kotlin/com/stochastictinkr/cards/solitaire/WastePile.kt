package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile(override val model: SolitaireModel) : CardSource {
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
        if (cards.size > 1 || cards.first() != this.cards.lastOrNull())
            throw IllegalArgumentException()
        this.cards.removeLast()
        return cards
    }

    fun returnCardsTo(stock: StockPile) {
        stock.setDeck(cards.asReversed())
        cards.clear()
    }

    fun onVisibleCard(block: (Card)->Unit) {
        cards.lastOrNull()?.let(block)
    }
}
