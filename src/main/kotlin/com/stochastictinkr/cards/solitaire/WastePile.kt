package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile(private val game: SolitaireGame) : CardSource {
    private val cards get() = game.currentState.waste

    override fun availableFrom(card: Card, state: SolitaireState): List<Card> =
        if (cards.lastOrNull() == card) listOf(card) else emptyList()

    fun onVisibleCard(block: (Card) -> Unit) {
        cards.lastOrNull()?.let(block)
    }

    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(cards.size == 1 && cards.first() == cards.lastOrNull())
        return target.receive(cards, state.removeFromWaste())
    }
}
