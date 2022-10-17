package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank
import com.stochastictinkr.cards.standard.CardSuit

class FoundationPile(private val ofSuit: CardSuit) : CardContainer {
    val cards = mutableListOf<Card>()
    fun canAdd(card: Card) = card.suit == ofSuit && isNext(card)
    private fun isNext(card: Card) = if (cards.isEmpty()) {
        card.rank == CardRank.ACE
    } else {
        cards.last().rank.isJustBefore(card.rank)
    }

    fun clear() {
        cards.clear()
    }

    override fun availableFrom(card: Card): List<Card> = emptyList()

    override fun take(cards: List<Card>): List<Card> {
        throw UnsupportedOperationException()
    }

    override fun canReceive(cards: List<Card>): List<Card> =
        cards.lastOrNull()
            ?.let { if (canAdd(it)) listOf(it) else null }
            ?: emptyList()

    override fun receive(cards: List<Card>) {
        if (cards.size != 1 || !canAdd(cards.first())) {
            throw IllegalArgumentException()
        }
        this.cards.add(cards.first())
    }
}
