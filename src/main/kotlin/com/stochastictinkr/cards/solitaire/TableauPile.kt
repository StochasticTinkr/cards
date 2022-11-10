package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank


class TableauPile(private val game: SolitaireGame, private val idx: Int) :
    CardSource, CardReceiver {
    val visibleCardCount get() = game.currentState.tableauVisible[idx].size
    val hiddenCardCount get() = game.currentState.tableauHidden[idx].size
    val isEmpty get() = game.currentState.tableauVisible[idx].isEmpty() && game.currentState.tableauHidden[idx].isEmpty()

    fun forEachVisibleCard(block: (Card) -> Unit) {
        game.currentState.tableauVisible[idx].forEach(block)
    }

    override fun transfer(cards: List<Card>, target: CardReceiver, state: SolitaireState): SolitaireState {
        require(game.currentState.tableauVisible[idx].endsWith(cards))
        return target.receive(cards, state.removeFromTableau(idx, cards.size))
    }

    fun forEachHiddenCard(block: (Card) -> Unit) {
        game.currentState.tableauHidden[idx].forEach(block)
    }

    private fun canAdd(card: Card, state: SolitaireState): Boolean {
        val (_, rank, color) = card
        return when {
            isEmpty -> rank == CardRank.KING
            else -> state.tableauVisible[idx].last().let { (_, topRank, topColor) ->
                topColor != color && topRank.isJustAfter(rank)
            }
        }
    }

    override fun availableFrom(card: Card, state: SolitaireState): List<Card> {
        if (state.tableauHidden[idx].contains(card)) {
            return state.tableauVisible[idx]
        }
        return state.tableauVisible[idx].asSequence().dropWhile { it != card }.toList()
    }

    override fun receive(cards: List<Card>, state: SolitaireState): SolitaireState {
        require(cards.isValidTableau && cards.firstOrNull()?.let { canAdd(it, game.currentState) } == true)
        return state.addToTableau(idx, cards)
    }

    override fun canReceive(cards: List<Card>, state: SolitaireState): List<Card> {
        val addableList = cards
            .asSequence()
            .dropWhile { !canAdd(it, game.currentState) }
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
