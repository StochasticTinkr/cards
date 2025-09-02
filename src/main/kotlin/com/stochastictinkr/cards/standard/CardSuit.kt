package com.stochastictinkr.cards.standard

enum class CardSuit(val color: CardColor, val suitName: String) {
    SPADES(CardColor.BLACK, "spades"),
    DIAMONDS(CardColor.RED, "diamonds"),
    CLUBS(CardColor.BLACK, "clubs"),
    HEARTS(CardColor.RED, "hearts"),
}