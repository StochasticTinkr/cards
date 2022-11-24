package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

object Foundations : CardReceiver {
    override fun canReceive(cards: List<Card>, state: SolitaireState): List<Card> {
        return cards.lastOrNull()
            .let {
                if (it != null && state.foundationCanReceive(it)) {
                    listOf(it)
                } else
                    emptyList()
            }
    }

    override fun receive(cards: List<Card>, state: SolitaireState): SolitaireState {
        require(cards.size == 1)
        return state.addToFoundation(cards.last())
    }
}