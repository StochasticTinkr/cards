package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile : CardContainer {
    val cards = mutableListOf<Card>()
    fun add(card: Card) {
        cards.add(card)
    }

    override fun availableFrom(card: Card): List<Card> =
        if (cards.lastOrNull() == card) listOf(card) else emptyList()

    override fun take(cards: List<Card>): List<Card> {
        if (cards.size > 1 || cards.first() != this.cards.firstOrNull())
            throw IllegalArgumentException()
        this.cards.removeLast()
        return cards
    }

    override fun canReceive(cards: List<Card>): List<Card> = emptyList()

    override fun receive(cards: List<Card>) {
        throw UnsupportedOperationException()
    }
}
