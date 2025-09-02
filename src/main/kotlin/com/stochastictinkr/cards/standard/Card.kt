package com.stochastictinkr.cards.standard

/**
 * A playing card with a suit and rank.
 * @property suit The suit of the card.
 * @property rank The rank of the card.
 */
data class Card(val suit: CardSuit, val rank: CardRank) {
    val color get() = suit.color
    val name get() = "${rank.rankName} of ${suit.suitName}"
    operator fun component3() = color
}
