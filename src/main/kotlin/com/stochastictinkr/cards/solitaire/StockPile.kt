package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class StockPile  {
    val cards = mutableListOf<CardModel>()

    fun removeTop() = cards.removeLast()
}

