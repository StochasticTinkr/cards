package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface CardContainer {
    fun availableFrom(card: Card): List<Card>
    fun take(cards: List<Card>): List<Card>
    fun canReceive(cards: List<Card>): List<Card>
    fun receive(cards: List<Card>)
}
