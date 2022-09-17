package com.stochastictinkr.cards.drawing

import java.awt.Shape
import java.awt.geom.Path2D

object Pips {
    val diamond: Shape = Path2D.Double().apply {
        val bot = +0.50
        val rgt = +0.28
        val top = -bot
        val lft = -rgt
        val mid = 0.00

        val bot2 = bot * .3
        val rgt2 = rgt * .3
        val top2 = -bot2
        val lft2 = -rgt2


        moveTo(+mid, top)
        quadTo(lft2, top2, lft, mid)
        quadTo(lft2, bot2, mid, bot)
        quadTo(rgt2, bot2, rgt, mid)
        quadTo(rgt2, top2, mid, top)
        closePath()
    }

    val clubs: Shape = Path2D.Double().apply {

        moveTo(+0.0, -0.0)
        curveTo(0.0, 0.5, 0.5, 0.3, 0.4, 0.0) // right bulb bottom
        curveTo(0.35, -0.2, 0.0, -0.3, 0.0, 0.0)  // right bulb top

        curveTo(0.0, -0.3, -0.35, -0.2, -0.4, 0.0) // left bulb top
        curveTo(-0.5, 0.3, 0.0, 0.5, 0.0, 0.0) // left bulb bottom

        curveTo(0.4, -0.3, 0.2, -0.5, 0.0, -.5) // top bulb right
        curveTo(-0.2, -0.5, -0.4, -0.3, 0.0, 0.0) // top  bulb left


        lineTo(+0.0, -0.0)
        lineTo(-.05, 0.15)
        quadTo(0.0, 0.48, -0.10, 0.48)
        lineTo(-.11, .5)
        lineTo(+.11, .5)
        lineTo(+.10, .48)
        quadTo(0.0, 0.48, +0.05, +0.15)
        lineTo(0.0, 0.0)
        closePath()
    }
    val heart: Shape = Path2D.Double().apply {
        val top = +0.5
        val rgt = 0.40
        val lft = -rgt
        val mid = 0.0

        moveTo(mid, top)
        curveTo(-0.10, +0.25, -0.25, +0.15, lft, -0.10)
        curveTo(-0.50, -0.25, lft, -0.50, -0.15, -0.5)
        curveTo(-0.00, -0.5, -0.00, -0.30, mid, -0.25)
        curveTo(-0.00, -0.3, -0.00, -0.50, +0.15, -0.50)
        curveTo(rgt, -0.50, +0.50, -0.25, rgt, -0.10)
        curveTo(+0.25, +0.15, +0.10, +0.25, mid, top)
        closePath()
    }
    val spades: Shape = Path2D.Double().apply {
        moveTo(+0.0, -0.5) // top point
        curveTo(-0.10, -0.35, -0.25, -0.15, -0.30, +0.00) // top to left side
        curveTo(-0.40, +0.25, -0.20, +0.30, -0.15, +0.30) // left to left bottom
        curveTo(-0.00, +0.3, -0.00, +0.25, -0.0, +0.00) // left bottom to mid
        lineTo(-.02, 0.15)
        quadTo(0.0, 0.48, -0.10, 0.48)
        lineTo(-.11, .5)
        lineTo(+.11, .5)
        lineTo(+.10, .48)
        quadTo(0.0, 0.48, +0.02, +0.15)
        lineTo(0.0, 0.0)

        curveTo(-0.00, +0.25, -0.00, +0.30, +0.15, +0.30) // mid to right bottom
        curveTo(+0.20, +0.30, +0.40, +0.25, +0.30, +0.00) // right bottom to right side
        curveTo(+0.25, -0.15, +0.10, -0.35, +0.0, -0.5) // right side to top
        closePath()
    }
}

