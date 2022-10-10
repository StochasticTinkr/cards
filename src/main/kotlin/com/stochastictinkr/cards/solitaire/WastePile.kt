package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile {
    val cards = mutableListOf<Card>()
    fun add(card: Card) {
        cards.add(card)
    }
}
