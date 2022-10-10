package com.stochastictinkr.cards.solitaire

class StockPile  {
    val cards = mutableListOf<Card>()

    fun removeTop() = cards.removeLast()
}

