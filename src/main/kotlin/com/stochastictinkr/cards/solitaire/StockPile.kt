package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class StockPile(override val model: SolitaireModel) : CardLocation  {
    val numberOfCards get() = cards.size
    val isEmpty get()= cards.isEmpty()
    private val cards = ArrayList<Card>(52)
    fun dealFaceUpTo(tableauPile: TableauPile) {
        tableauPile.addVisibleCard(cards.removeLast())
    }

    fun dealFaceDownTo(tableauPile: TableauPile) {
        tableauPile.addHiddenCard(cards.removeLast())
    }

    fun dealFaceUpTo(wastePile: WastePile) {
        cards.removeLastOrNull()?.let(wastePile::add)
    }

    fun setDeck(deck: List<Card>) {
        cards.clear()
        cards.addAll(deck)
    }

    fun forEachCard(block: (Card) -> Unit) {
        cards.forEach(block)
    }
}

