package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class StockPile  {
    val cards = mutableListOf<Card>()

    fun removeTop() = cards.removeLast()
}

