package com.stochastictinkr.cards.standard


data object StandardDeck {
    val cards = CardSuit.entries.flatMap { suit ->
        CardRank.entries.map { rank ->
            Card(suit, rank)
        }
    }
}
