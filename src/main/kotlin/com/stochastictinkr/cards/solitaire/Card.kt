package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.DeckDefinition

class Card(val cardDefinition: DeckDefinition.Card, val rank: Int) {
    val suit get() = cardDefinition.suit!!
    val color get() = cardDefinition.color!!
    val image get() = cardDefinition.image
}