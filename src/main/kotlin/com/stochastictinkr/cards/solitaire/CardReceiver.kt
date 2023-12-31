package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface CardReceiver  {
    fun canReceive(cards: List<Card>, state: SolitaireState): List<Card>
    fun receive(cards: List<Card>, state: SolitaireState): SolitaireState
}