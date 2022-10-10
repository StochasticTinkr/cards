package com.stochastictinkr.cards.solitaire

private const val KING_RANK = 13

class TableauPile {
    val visibleCards = mutableListOf<Card>()
    val hiddenCards = mutableListOf<Card>()
    val isEmpty get() = visibleCards.isEmpty() && hiddenCards.isEmpty()

    fun canAdd(card: Card): Boolean {
        return when {
            isEmpty -> card.rank == KING_RANK
            else -> visibleCards.last().let { lastCard ->
                lastCard.color != card.color && lastCard.rank + 1 == card.rank
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
