package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface SolitaireListener {
    fun newGame(state: SolitaireState)
    fun stateChanged(oldState: SolitaireState, newState: SolitaireState)
    fun selectionChanged(cardSource: CardSource, selectedCards: List<Card>)
    fun selectionCleared()
}
