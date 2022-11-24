package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card


class TableauPile(private val game: SolitaireGame, private val idx: Int) :
    CardSource {
    fun forEachVisibleCard(block: (Card) -> Unit) {
        game.currentState.tableauVisible[idx].forEach(block)
    }

    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(game.currentState.tableauVisible[idx].endsWith(cards))
        return target.receive(cards, state.removeFromTableau(idx, cards.size))
    }

    fun forEachHiddenCard(block: (Card) -> Unit) {
        game.currentState.tableauHidden[idx].forEach(block)
    }

    override fun availableFrom(card: Card, state: SolitaireState): List<Card> {
        if (state.tableauHidden[idx].contains(card)) {
            return state.tableauVisible[idx]
        }
        return state.tableauVisible[idx].asSequence().dropWhile { it != card }.toList()
    }


    private fun <T> List<T>.endsWith(tail: List<T>) = tail.size <= size && subList(size - tail.size, size) == tail
}
