package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank


class TableauPile(val solitaireListener: SolitaireListener) : CardSource, CardReceiver {
    private val visibleCards = mutableListOf<Card>()
    private val hiddenCards = mutableListOf<Card>()
    val visibleCardCount get() = visibleCards.size
    val hiddenCardCount get() = hiddenCards.size
    val isEmpty get() = visibleCards.isEmpty() && hiddenCards.isEmpty()

    fun forEachVisibleCard(block: (Card) -> Unit) {
        visibleCards.forEach(block)
    }

    fun forEachHiddenCard(block: (Card) -> Unit) {
        hiddenCards.forEach(block)
    }

    private fun canAdd(card: Card): Boolean {
        val (_, rank, color) = card
        return when {
            isEmpty -> rank == CardRank.KING
            else -> visibleCards.last().let { (_, topRank, topColor) ->
                topColor != color && topRank.isJustAfter(rank)
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

    override fun availableFrom(card: Card): List<Card> {
        if (hiddenCards.contains(card)) {
            return visibleCards
        }
        return visibleCards.asSequence().dropWhile { it != card }.toList()
    }

    override fun take(cards: List<Card>): List<Card> {
        require(
            isTailOfVisibleCards(cards)
        )
        visibleCards.subList(visibleCards.size - cards.size, visibleCards.size).clear()
        if (visibleCards.isEmpty()) {
            hiddenCards.removeLastOrNull()?.let { visibleCards.add(it) }
        }
        return cards
    }

    private fun isTailOfVisibleCards(cards: List<Card>) = (
            visibleCards.size >= cards.size
                    && visibleCards.subList(visibleCards.size - cards.size, visibleCards.size) == cards
            )

    override fun canReceive(cards: List<Card>): List<Card> {
        val addableList = cards
            .asSequence()
            .dropWhile { !canAdd(it) }
            .toList()

        return if (addableList.isValidTableau) {
            addableList
        } else {
            emptyList()
        }
    }

    override fun receive(cards: List<Card>) {
        require(cards.isValidTableau && cards.firstOrNull()?.let { canAdd(it) } == true)
        visibleCards.addAll(cards)
    }

    private val List<Card>.isValidTableau: Boolean
        get() {
            if (size < 2) {
                return true
            }
            for (i in 0 until (size - 1)) {
                val a = this[i]
                val b = this[i + 1]
                if (a.suit == b.suit) {
                    return false
                }
                if (!a.rank.isJustAfter(b.rank)) {
                    return false
                }
            }
            return true
        }
}
