package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

object WastePile : CardSource {
    override fun availableFrom(card: Card, state: SolitaireState): List<Card> =
        if (state.waste.lastOrNull() == card) listOf(card) else emptyList()

    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(cards.size == 1 && cards.first() == cards.lastOrNull())
        return target.receive(cards, state.removeFromWaste())
    }
}
