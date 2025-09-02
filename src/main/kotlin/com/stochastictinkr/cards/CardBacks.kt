package com.stochastictinkr.cards

/**
 * The available card backs images. See [CardImages] for implementation details.
 * @property filename The partial filename of the SVG file (without path and extension).
 * @property displayName The display name of the card back for use in UIs.
 */
enum class CardBacks(val filename: String, val displayName: String) {
    ABSTRACT("abstract", "Abstract"),
    CLOUDS("abstract_clouds", "Abstract Clouds"),
    HIGHWAY("abstract_scene", "Abstract Scene"),
    ASTRONAUT("astronaut", "Astronaut"),
    BLUE("blue", "Blue"),
    BLUE2("blue2", "Blue2"),
    CARS("cars", "Cars"),
    CASTLE("castle", "Castle"),
    FISH("fish", "Fish"),
    FROG("frog", "Frog"),
    RED("red", "Red"),
    RED2("red2", "Red2"),
}