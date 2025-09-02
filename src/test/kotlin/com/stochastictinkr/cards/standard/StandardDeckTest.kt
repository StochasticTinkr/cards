package com.stochastictinkr.cards.standard

import kotlin.test.*

class StandardDeckTest {
    @Test
    fun `standard deck has 52 unique cards`() {
        val cards = StandardDeck.cards
        assertEquals(52, cards.size, "Deck should have 52 cards")
        assertEquals(52, cards.toSet().size, "Deck should have 52 unique cards")
    }
}
