package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank
import com.stochastictinkr.cards.standard.CardSuit

class FoundationPile(private val ofSuit: CardSuit, override val model: SolitaireModel) : CardReceiver {
    private val cards = mutableListOf<Card>()
    val visibleCard get() = cards.lastOrNull()

    inline fun <T> onVisibleCard(block: (Card) -> T): T? = visibleCard?.let(block)

    private fun canAdd(card: Card) = card.suit == ofSuit && isNext(card)
    private fun isNext(card: Card) =
        if (cards.isEmpty()) card.rank == CardRank.ACE else cards.last().rank.isJustBefore(card.rank)

    fun clear() = cards.clear()

    override fun canReceive(cards: List<Card>): List<Card> =
        if (cards.isNotEmpty() && canAdd(cards.last())) {
            listOf(cards.last())
        } else {
            emptyList()
        }

    override fun receive(cards: List<Card>) {
        require(cards.size == 1 && canAdd(cards.first()))
        this.cards.add(cards.first())
    }
}
