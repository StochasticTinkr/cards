package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class StockPile : CardContainer {
    val cards = mutableListOf<Card>()

    fun removeTop() = cards.removeLast()
    override fun availableFrom(card: Card): List<Card> = emptyList()
    override fun take(cards: List<Card>): List<Card> = emptyList()
    override fun canReceive(cards: List<Card>): List<Card> = emptyList()
    override fun receive(cards: List<Card>) {}
}

