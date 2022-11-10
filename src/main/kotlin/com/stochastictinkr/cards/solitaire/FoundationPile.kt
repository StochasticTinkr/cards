package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.CardRank
import com.stochastictinkr.cards.standard.CardSuit

class FoundationPile(private val ofSuit: CardSuit, private val game: SolitaireGame) : CardReceiver {
    val visibleCard get() = game.currentState.foundations[ofSuit.ordinal].lastOrNull()

    inline fun <T> onVisibleCard(block: (Card) -> T): T? = visibleCard?.let(block)

    private fun canAdd(card: Card, state: SolitaireState) = card.suit == ofSuit && isNext(card, state)
    private fun isNext(card: Card, state: SolitaireState) =
        if (state.foundations[ofSuit.ordinal].isEmpty()) card.rank == CardRank.ACE else state.foundations[ofSuit.ordinal].last().rank.isJustBefore(
            card.rank
        )

    override fun canReceive(cards: List<Card>, state: SolitaireState): List<Card> =
        if (cards.isNotEmpty() && canAdd(cards.last(), state)) {
            listOf(cards.last())
        } else {
            emptyList()
        }

    override fun receive(cards: List<Card>, state: SolitaireState): SolitaireState {
        require(cards.size == 1 && canAdd(cards.first(), state))
        return state.addToFoundation(ofSuit.ordinal, cards.first())
    }
}
