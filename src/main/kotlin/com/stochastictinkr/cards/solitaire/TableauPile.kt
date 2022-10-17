package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank


class TableauPile : CardContainer {
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

    override fun availableFrom(card: Card): List<Card> {
        if (hiddenCards.contains(card)) {
            return visibleCards
        }
        return visibleCards.asSequence().dropWhile { it != card }.toList()
    }

    override fun take(cards: List<Card>): List<Card> {
        if (visibleCards.size < cards.size ||
            visibleCards.subList(visibleCards.size - cards.size, visibleCards.size) != cards
        ) throw IllegalArgumentException()
        if (visibleCards.size == cards.size) {
            visibleCards.clear()
            hiddenCards.removeLastOrNull()?.let { visibleCards.add(it) }
            return cards
        }
        visibleCards.subList(visibleCards.size - cards.size, visibleCards.size).clear()
        return cards
    }

    override fun canReceive(cards: List<Card>): List<Card> =
        cards
            .asSequence()
            .dropWhile { !canAdd(it) }
            .toList()
            .let {
                if (it.isValidTableau) {
                    it
                } else {
                    emptyList()
                }
            }

    override fun receive(cards: List<Card>) {
        if (!cards.isValidTableau || cards.firstOrNull()?.let { canAdd(it) } != true) {
            throw IllegalArgumentException()
        }
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
