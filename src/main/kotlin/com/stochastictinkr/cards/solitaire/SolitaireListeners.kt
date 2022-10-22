package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

class SolitaireListeners : SolitaireListener {
    private val listeners = mutableListOf<SolitaireListener>()
    private inline fun dispatch(block: (SolitaireListener) -> Unit) {
        listeners.forEach(block)
    }

    override fun cardDealtFaceUp(stockPile: StockPile, card: Card, cardReceiver: CardReceiver) {
        dispatch { it.cardDealtFaceUp(stockPile, card, cardReceiver) }
    }

    override fun cardDealtFaceDown(stockPile: StockPile, card: Card, cardReceiver: CardReceiver) {
        dispatch { it.cardDealtFaceDown(stockPile, card, cardReceiver) }
    }

    override fun wasteRestocked(wastePile: WastePile, cards: List<Card>, stockPile: StockPile) {
        dispatch { it.wasteRestocked(wastePile, cards, stockPile) }
    }

    override fun cardsMoved(cardSource: CardSource, cards: List<Card>, cardReceiver: CardReceiver) {
        dispatch { it.cardsMoved(cardSource, cards, cardReceiver) }
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