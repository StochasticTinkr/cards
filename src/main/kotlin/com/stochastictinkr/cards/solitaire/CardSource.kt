package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface CardSource : CardLocation {
    fun availableFrom(card: Card): List<Card> = emptyList()
    fun take(cards: List<Card>): List<Card> = emptyList()
}
