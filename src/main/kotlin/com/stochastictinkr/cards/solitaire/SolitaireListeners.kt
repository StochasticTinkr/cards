package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class SolitaireListeners : SolitaireListener {
    private val listeners = mutableListOf<SolitaireListener>()
    private inline fun dispatch(block: (SolitaireListener) -> Unit) {
        listeners.forEach(block)
    }

    override fun newGame(state: SolitaireState) {
        dispatch { it.newGame(state) }
    }

    override fun stateChanged(oldState: SolitaireState, newState: SolitaireState) {
        dispatch { it.stateChanged(oldState, newState) }
    }

    override fun selectionChanged(cardSource: CardSource, selectedCards: List<Card>) {
        dispatch { it.selectionChanged(cardSource, selectedCards) }
    }
    override fun selectionCleared() {
        dispatch { it.selectionCleared() }
    }

    fun add(listener: SolitaireListener) {
        listeners.add(listener)
    }
    fun remove(listener: SolitaireListener) {
        listeners.remove(listener)
    }
}