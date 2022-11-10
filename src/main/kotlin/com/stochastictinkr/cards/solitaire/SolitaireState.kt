package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

private typealias Cards = List<Card>

data class SolitaireState(
    val foundations: List<Cards>,
    val tableauHidden: List<Cards>,
    val tableauVisible: List<Cards>,
    val stock: Cards,
    val waste: Cards,
) {
    fun removeFromTableau(idx: Int, numCards: Int) =
        if (tableauVisible.size < numCards || tableauHidden.isEmpty()) {
            copy(
                tableauVisible = tableauVisible.modify(idx) { it.subList(0, it.size - numCards) })
        } else {
            copy(
                tableauVisible = tableauVisible.modify(idx) { listOf(tableauHidden[idx].last()) },
                tableauHidden = tableauHidden.modify(idx) { it.subList(0, it.size - 1) }
            )
        }

    fun addToTableau(idx: Int, cards: Cards) =
        copy(
            tableauVisible = tableauVisible.modify(idx) { it + cards }
        )

    fun addToFoundation(idx: Int, card: Card) =
        copy(
            foundations = foundations.modify(idx) { it + card }
        )

    fun removeFromWaste() =
        copy(waste = waste.subList(0, waste.size - 1))

    fun pullFromStock() = if (stock.isEmpty()) {
        if (waste.isEmpty()) {
            this
        } else {
            val cards = waste.reversed()
            copy(stock = cards.subList(0, cards.size - 1), waste = listOf(cards.last()))
        }
    } else {
        val cards = stock
        copy(stock = cards.subList(0, cards.size - 1), waste = waste + cards.last())
    }


    private inline fun <T> List<T>.modify(idx: Int, block: (T) -> T) =
        mapIndexed { index, item ->
            if (index == idx) block(item) else item
        }
}