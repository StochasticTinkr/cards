package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class WastePile(private val game: SolitaireGame) : CardSource {
    val cards get() = game.state.waste
    override fun availableFrom(card: Card): List<Card> =
        if (game.state.waste.lastOrNull() == card) listOf(card) else emptyList()

    fun onVisibleCard(block: (Card) -> Unit) {
        game.state.waste.lastOrNull()?.let(block)
    }

    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(cards.size == 1 && cards.first() == this.cards.lastOrNull())
        return target.receive(cards, state.removeFromWaste())
    }
}
