package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank


class TableauPile {
    val visibleCards = mutableListOf<Card>()
    val hiddenCards = mutableListOf<Card>()
    val isEmpty get() = visibleCards.isEmpty() && hiddenCards.isEmpty()

    fun canAdd(card: Card): Boolean {
        val (_, rank, color) = card
        return when {
            isEmpty -> rank == CardRank.KING
            else -> visibleCards.last().let { (_, topRank, topColor) ->
                topColor != color && topRank.isJustBefore(rank)
            }
        }
    }

    fun clear() {
        visibleCards.clear()
        hiddenCards.clear()
    }

    fun addVisibleCard(card: Card) {
        visibleCards.add(card)
    }

    fun addHiddenCard(card: Card) {
        hiddenCards.add(card)
    }


}
