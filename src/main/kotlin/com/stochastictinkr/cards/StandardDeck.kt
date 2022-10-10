package com.stochastictinkr.cards

fun standardDeck(includeJokers: Boolean = false): DeckDefinition {
    val red = DeckDefinition.Color("red")
    val black = DeckDefinition.Color("black")
    val colors = listOf(red, black)

    val suits = listOf(
        DeckDefinition.Suit("diamonds", red),
        DeckDefinition.Suit("clubs", black),
        DeckDefinition.Suit("hearts", red),
        DeckDefinition.Suit("spades", black),
    )
    val indexes = listOf(
        "ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king"
    ).map(DeckDefinition::Index)
    val cards = sequence {
        suits.forEach { suit ->
            indexes.forEach { index ->
                yield(
                    DeckDefinition.Card(
                        suit, index, suit.color, "${index.name} of ${suit.name}"
                    )
                )
            }
        }
        if (includeJokers) {
            val jokerIndex = DeckDefinition.Index("joker")
            colors.forEach { color ->
                yield(
                    DeckDefinition.Card(
                        suit = null, jokerIndex, color, "${color.name} ${jokerIndex.name}"
                    )
                )
            }
        }
    }.toList()

    return DeckDefinition(colors, suits, indexes, cards)
}
