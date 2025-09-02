package com.stochastictinkr.cards.standard

enum class CardRank(val rankName: String) {
    ACE("ace"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    JACK("jack"),
    QUEEN("queen"),
    KING("king");

    val previous: CardRank? get() = if (ordinal == 0) null else CardRank.entries[ordinal - 1]

    fun isJustAfter(other: CardRank) = ordinal == other.ordinal + 1
    fun isJustBefore(other: CardRank) = other.isJustAfter(this)
}