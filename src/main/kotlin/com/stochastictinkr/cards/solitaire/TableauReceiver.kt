package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank

class TableauReceiver(val idx: Int) : CardReceiver {
    override fun receive(cards: List<Card>, state: SolitaireState): SolitaireState {
        require(cards.isValidTableau && cards.firstOrNull()?.let { canAdd(it, state) } == true)
        return state.addToTableau(idx, cards)
    }

    override fun canReceive(cards: List<Card>, state: SolitaireState): List<Card> {
        val addableList = cards
            .asSequence()
            .dropWhile { !canAdd(it, state) }
            .toList()

        return if (addableList.isValidTableau) {
            addableList
        } else {
            emptyList()
        }
    }

    private fun canAdd(card: Card, state: SolitaireState): Boolean {
        val (_, rank, color) = card
        return when {
            state.tableauVisible[idx].isEmpty() && state.tableauHidden[idx].isEmpty() -> rank == CardRank.KING
            else -> state.tableauVisible[idx].last().let { (_, topRank, topColor) ->
                topColor != color && topRank.isJustAfter(rank)
            }
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
                if (a.color == b.color) {
                    return false
                }
                if (!a.rank.isJustAfter(b.rank)) {
                    return false
                }
            }
            return true
        }

}
