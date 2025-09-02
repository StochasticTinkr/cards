package com.stochastictinkr.cards

import com.stochastictinkr.cards.standard.*
import com.stochastictinkr.svg.*
import org.apache.batik.gvt.*
import java.awt.image.*
import java.io.*
import kotlin.math.*

/**
 * Manages loading and caching of card images for both fronts and backs of the standard deck.
 * Card images are loaded from SVG files and rendered to `BufferedImage` objects.
 */
class CardImages {
    private val cardPainters = StandardDeck.cards.associateWith { card ->
        createImagePainter(card.imageFile)
    }

    private val backPainter = CardBacks.entries.associateWith {
        createImagePainter("cards/backs/${it.filename}.svg")
    }

    /**
     * The aspect ratio (width / height) of the card images, derived from the first card's dimensions.
     */
    val cardAspectRatio = cardPainters.values.first().bounds.run { width / height }

    /**
     * The width of the card images in pixels. Setting this property will clear the image cache.
     * The height is automatically adjusted to maintain the aspect ratio.
     */
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

    /**
     * The height of the card images in pixels. Setting this property will clear the image cache.
     * The width is automatically adjusted to maintain the aspect ratio.
     */
    var cardHeight: Int
        get() = max(1, (cardWidth / cardAspectRatio).roundToInt())
        set(value) {
            cardWidth = (value * cardAspectRatio).roundToInt()
        }

    private val cache = mutableMapOf<Any, BufferedImage>()

    /**
     * Retrieves the image for the specified card, rendering it if not already cached.
     * @param card The card for which to retrieve the image.
     * @return A `BufferedImage` representing the card's front.
     */
    operator fun get(card: Card): BufferedImage = cache.computeIfAbsent(card) {
        BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_ARGB).also {
            cardPainters.getValue(card).drawTo(it)
        }
    }

    /**
     * Retrieves the image for the specified card back, rendering it if not already cached.
     * @param back The card back design to retrieve.
     * @return A `BufferedImage` representing the card's back.
     */
    operator fun get(back: CardBacks) = cache.computeIfAbsent(back) {
        BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_ARGB).also {
            backPainter.getValue(back).drawTo(it)
        }
    }

    private fun createImagePainter(resourceName: String): GraphicsNode {
        val resource = {}.javaClass.classLoader.getResource(resourceName) ?: throw FileNotFoundException(resourceName)
        return loadSvgGraphicsNode(resource.toExternalForm(), resource.openStream())
    }

    private val Card.imageFile get() = "cards/fronts/${suit.suitName}_${rank.rankName}.svg"

}

