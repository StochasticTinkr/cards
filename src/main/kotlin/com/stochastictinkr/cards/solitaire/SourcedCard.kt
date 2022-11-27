package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.standard.Card

data class SourcedCard(val source: CardSource, val card: Card)