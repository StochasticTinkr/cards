package com.stochastictinkr.cards.standard

data class Card(val suit: CardSuit, val rank: CardRank) {
    val color get() = suit.color
    val name get() = "${rank.rankName} of ${suit.suitName}"
    operator fun component3() = color
}
