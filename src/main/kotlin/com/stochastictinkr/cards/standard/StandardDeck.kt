package com.stochastictinkr.cards.standard


object StandardDeck {
    val cards = CardSuit.values().flatMap { suit ->
        CardRank.values().map { rank ->
            Card(suit, rank)
        }
    }
}
