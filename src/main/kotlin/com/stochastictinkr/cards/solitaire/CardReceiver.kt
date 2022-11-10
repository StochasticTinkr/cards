package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface CardReceiver : CardLocation {
    fun canReceive(cards: List<Card>): List<Card>
    fun receive(cards: List<Card>, state: SolitaireState): SolitaireState
}