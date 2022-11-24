package com.stochastictinkr.cards

import com.stochastictinkr.cards.standard.Card
import com.stochastictinkr.cards.standard.StandardDeck
import com.stochastictinkr.svg.drawTo
import com.stochastictinkr.svg.loadSvgGraphicsNode
import org.apache.batik.gvt.GraphicsNode
import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import kotlin.math.max
import kotlin.math.roundToInt

class CardImages {
    private val cardPainters = StandardDeck.cards.associateWith { card ->
        createImagePainter(card.imageFile)
    }

    private val backPainter = CardBacks.values().associateWith {
        createImagePainter("cards/backs/${it.filename}.svg")
    }

    val cardAspectRatio = cardPainters.values.first().bounds.run { width / height }
    var cardWidth: Int = cardPainters.values.first().bounds.width.toInt()
        set(value) {
            if (value <= 0) {
                if (field != 1) {
                    field = 1
                    cache.clear()
                    return
                }
            }
            if (field != value) {
                cache.clear()
                field = value
            }
        }
    var cardHeight: Int
        get() = max(1, (cardWidth / cardAspectRatio).roundToInt())
        set(value) {
            cardWidth = (value * cardAspectRatio).roundToInt()
        }

    private val cache = mutableMapOf<Any, BufferedImage>()

    operator fun get(card: Card): BufferedImage = cache.computeIfAbsent(card) {
        BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_ARGB).also {
            cardPainters[card]!!.drawTo(it)
        }
    }

    operator fun get(back: CardBacks) = cache.computeIfAbsent(back) {
        BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_ARGB).also {
            backPainter[back]!!.drawTo(it)
        }
    }

    private fun createImagePainter(resourceName: String): GraphicsNode {
        val resource = {}.javaClass.classLoader.getResource(resourceName) ?: throw FileNotFoundException(resourceName)
        return loadSvgGraphicsNode(resource.toExternalForm(), resource.openStream())
    }

    private val Card.imageFile get() = "cards/fronts/${suit.suitName}_${rank.rankName}.svg"

}

