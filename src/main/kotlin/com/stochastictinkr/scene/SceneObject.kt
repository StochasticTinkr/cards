package com.stochastictinkr.scene

import java.awt.geom.Rectangle2D

interface SceneObject {
    val zOrder: Double
    val bounds: Rectangle2D
    val isDraggable: Boolean
}
