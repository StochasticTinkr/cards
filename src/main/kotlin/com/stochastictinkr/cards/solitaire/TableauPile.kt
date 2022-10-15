package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank


class TableauPile {
    val visibleCards = mutableListOf<CardModel>()
    val hiddenCards = mutableListOf<CardModel>()
    val isEmpty get() = visibleCards.isEmpty() && hiddenCards.isEmpty()

    fun canAdd(card: CardModel): Boolean {
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

    fun addVisibleCard(card: CardModel) {
        visibleCards.add(card)
    }

    fun addHiddenCard(card: CardModel) {
        hiddenCards.add(card)
    }


}
