package com.stochastictinkr.cards.standard

/**
 * The suit of a playing card.
 * @property color The color of the suit.
 * @property suitName The name of the suit.
 */
enum class CardSuit(val color: CardColor, val suitName: String) {
    SPADES(CardColor.BLACK, "spades"),
    DIAMONDS(CardColor.RED, "diamonds"),
    CLUBS(CardColor.BLACK, "clubs"),
    HEARTS(CardColor.RED, "hearts"),
}