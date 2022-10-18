package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface CardReceiver : CardLocation {
    fun canReceive(cards: List<Card>): List<Card> = emptyList()
    fun receive(cards: List<Card>): Unit = throw UnsupportedOperationException()
}