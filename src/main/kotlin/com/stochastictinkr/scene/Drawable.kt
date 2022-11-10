package com.stochastictinkr.scene

import java.awt.Graphics2D

interface Drawable : SceneObject {
    fun draw(g: Graphics2D)
}
