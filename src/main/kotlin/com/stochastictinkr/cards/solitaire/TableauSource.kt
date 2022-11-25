package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card


class TableauSource(private val idx: Int) : CardSource {
    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(state.tableauVisible[idx].endsWith(cards))
        return target.receive(cards, state.removeFromTableau(idx, cards.size))
    }

    override fun availableFrom(card: Card, state: SolitaireState): List<Card> {
        if (state.tableauHidden[idx].contains(card)) {
            return state.tableauVisible[idx]
        }
        return state.tableauVisible[idx].asSequence().dropWhile { it != card }.toList()
    }

    private fun <T> List<T>.endsWith(tail: List<T>) = tail.size <= size && subList(size - tail.size, size) == tail
}
