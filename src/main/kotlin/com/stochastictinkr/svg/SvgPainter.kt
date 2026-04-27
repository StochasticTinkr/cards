package com.stochastictinkr.svg

import java.awt.geom.*
import java.awt.image.*

interface SvgPainter {
    val bounds: Rectangle2D
    fun drawTo(image: BufferedImage)
}


