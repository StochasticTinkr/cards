package com.stochastictinkr.cards

data class DeckDefinition(
    val colors: List<Color>,
    val suits: List<Suit>,
    val indices: List<Index>,
    val cards: List<Card>,
) {
    data class Card(
        val suit: Suit?,
        val index: Index?,
        val color: Color? = suit?.color,
        val name: String,
    )

    data class Color(val name: String)
    data class Suit(val name: String, val color: Color? = null)
    data class Index(val name: String)
}

