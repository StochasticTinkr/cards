package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

interface SolitaireListener {
    fun cardDealtFaceUp(stockPile: StockPile, card: Card, cardReceiver: CardReceiver)
    fun cardDealtFaceDown(stockPile: StockPile, card: Card, cardReceiver: CardReceiver)
    fun wasteRestocked(wastePile: WastePile, cards: List<Card>, stockPile: StockPile)
    fun cardsMoved(cardSource: CardSource, cards: List<Card>, cardReceiver: CardReceiver)
    fun selectionChanged(cardSource: CardSource, selectedCards: List<Card>)
    fun selectionCleared()
}
