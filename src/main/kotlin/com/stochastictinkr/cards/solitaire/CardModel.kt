package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.skywing.awt.geom.point
import java.awt.geom.Point2D


class CardModel(val card: Card) {
    var position: Point2D = point(0.0, 0.0)
    var isVisible: Boolean = false
    var isSelected: Boolean = false
    val suit = card.suit
    val rank = card.rank
    val color = card.color

    operator fun component1() = suit
    operator fun component2() = rank
    operator fun component3() = color

    override fun equals(other: Any?): Boolean {
        return this === other || other is CardModel && suit == other.suit && rank == other.rank
    }

    override fun hashCode(): Int {
        return suit.suitName.hashCode() * 37 + rank.rankName.hashCode()
    }
}