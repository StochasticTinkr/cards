package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank


class TableauPile(val solitaireListener: SolitaireListener, private val game: SolitaireGame, private val idx: Int) :
    CardSource, CardReceiver {
    private val visibleCards get() = game.state.tableauVisible[idx]
    private val hiddenCards get() = game.state.tableauHidden[idx]
    val visibleCardCount get() = visibleCards.size
    val hiddenCardCount get() = hiddenCards.size
    val isEmpty get() = visibleCards.isEmpty() && hiddenCards.isEmpty()

    fun forEachVisibleCard(block: (Card) -> Unit) {
        visibleCards.forEach(block)
    }

    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(visibleCards.endsWith(cards))
        return target.receive(cards, state.removeFromTableau(idx, cards.size))
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

    override fun availableFrom(card: Card): List<Card> {
        if (hiddenCards.contains(card)) {
            return visibleCards
        }
        return visibleCards.asSequence().dropWhile { it != card }.toList()
    }

    override fun receive(cards: List<Card>, state: SolitaireState): SolitaireState {
        require(cards.isValidTableau && cards.firstOrNull()?.let { canAdd(it) } == true)
        return state.addToTableau(idx, cards)
    }

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

    private fun <T> List<T>.endsWith(tail: List<T>) = tail.size <= size && subList(size - tail.size, size) == tail
}
