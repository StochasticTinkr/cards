package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit

class FoundationPile(val ofSuit: CardSuit) {
    val cards = mutableListOf<Card>()
    fun canAdd(card: Card) = card.suit == ofSuit && isNext(card)
    private fun isNext(card: Card) = (cards.lastOrNull()?.rank?.ordinal ?: 0) + 1 == card.rank.ordinal
    fun clear() {
        cards.clear()
    }
}
