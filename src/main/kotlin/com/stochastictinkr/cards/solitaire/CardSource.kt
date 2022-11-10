package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface CardSource : CardLocation {
    fun availableFrom(card: Card, state: SolitaireState): List<Card>
    fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState
}
