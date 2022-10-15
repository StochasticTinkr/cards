package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile {
    val cards = mutableListOf<CardModel>()
    fun add(card: CardModel) {
        cards.add(card)
    }
}
