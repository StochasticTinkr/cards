package com.stochastictinkr.cards.standard

enum class CardRank(val rankName: String) {
    ACE("ace"),
    _2("2"),
    _3("3"),
    _4("4"),
    _5("5"),
    _6("6"),
    _7("7"),
    _8("8"),
    _9("9"),
    _10("10"),
    JACK("jack"),
    QUEEN("queen"),
    KING("king");

    val previous: CardRank? get() = if (ordinal == 0) null else values()[ordinal - 1]

    fun isJustAfter(other: CardRank) = ordinal == other.ordinal + 1
    fun isJustBefore(other: CardRank) = other.isJustAfter(this)
}