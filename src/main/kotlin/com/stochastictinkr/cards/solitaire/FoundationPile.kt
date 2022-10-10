package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.DeckDefinition

class FoundationPile(val ofSuit: DeckDefinition.Suit) {
    val cards = mutableListOf<Card>()
    fun canAdd(card: Card) = card.suit == ofSuit && isNext(card)
    private fun isNext(card: Card) = (cards.lastOrNull()?.rank ?: 0) + 1 == card.rank
    fun clear() {
        cards.clear()
    }
}
