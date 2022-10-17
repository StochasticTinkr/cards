package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardSuit
import com.stochastictinkr.cards.standard.StandardDeck
import kotlin.random.Random

class SolitaireModel() {
    var random = Random(System.nanoTime())
    var isGameActive = false
    val foundations = CardSuit.values().map { FoundationPile(it) }
    val stock = StockPile()
    val tableauPiles = List(7) { TableauPile() }
    val wastePile = WastePile()

    val selectedCards = mutableListOf<Card>()

    fun newGame() {
        foundations.forEach { it.clear() }
        tableauPiles.forEach { it.clear() }
        stock.cards.addAll(StandardDeck.cards.shuffled(random))

        for (i in 0 until 7) {
            tableauPiles[6-i].addVisibleCard(stock.removeTop())
            for (j in (i + 1) until 7) {
                tableauPiles[6 - j].addHiddenCard(stock.removeTop())
            }
        }
        wastePile.add(stock.removeTop())
        isGameActive = true
    }
}
