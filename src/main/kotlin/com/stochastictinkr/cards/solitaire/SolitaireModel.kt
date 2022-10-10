package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.DeckDefinition
import kotlin.random.Random

class SolitaireModel(val deck: DeckDefinition) {
    var random = Random(System.nanoTime())
    val allCards = deck.cards.map { Card(it, deck.indices.indexOf(it.index) + 1) }
    var isGameActive = false
    val foundations = Array(4) { FoundationPile(deck.suits[it]) }
    val stock = StockPile()
    val tableauPiles = Array(7) { TableauPile() }
    val wastePile = WastePile()

    fun newGame() {
        foundations.forEach { it.clear() }
        stock.cards.addAll(allCards.shuffled(random))
        tableauPiles.forEach { it.clear() }
        for (i in 0 until 7) {
            tableauPiles[i].addVisibleCard(stock.removeTop())
            for (j in (i + 1) until 7) {
                tableauPiles[j].addHiddenCard(stock.removeTop())
            }
        }
        wastePile.add(stock.removeTop())
        isGameActive = true
    }
}
