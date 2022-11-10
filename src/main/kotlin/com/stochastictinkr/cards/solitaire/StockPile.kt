package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class StockPile(val solitaireListener: SolitaireListener) : CardLocation {
    val numberOfCards get() = cards.size
    val isEmpty get() = cards.isEmpty()
    private val cards = ArrayList<Card>(52)
    fun dealFaceUpTo(tableauPile: TableauPile) {
        val card = cards.removeLast()
        solitaireListener.cardDealtFaceUp(this, card, tableauPile)
        tableauPile.addVisibleCard(card)
    }

    fun dealFaceDownTo(tableauPile: TableauPile) {
        tableauPile.addHiddenCard(cards.removeLast())
    }

    fun dealFaceUpTo(wastePile: WastePile) {
        cards.removeLastOrNull()?.let { card ->
            wastePile.add(card)
            solitaireListener.cardDealtFaceUp(this, card, wastePile)
        }
    }

    fun setDeck(deck: List<Card>) {
        cards.clear()
        cards.addAll(deck)
    }

    fun forEachCard(block: (Card) -> Unit) {
        cards.forEach(block)
    }
}

