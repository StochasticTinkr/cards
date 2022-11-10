package com.stochastictinkr.scene

import com.stochastictinkr.skywing.awt.geom.minus
import com.stochastictinkr.skywing.awt.geom.point
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Paint
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.geom.Point2D
import javax.swing.JComponent
import kotlin.math.abs
import kotlin.math.roundToInt

class JScene : JComponent() {
    private var _backgroundPaint: Paint? = null
    private var dragging: Dragging? = null

    private data class Dragging(val sceneObject: SceneObject, val startPoint: Point)

    var backgroundPaint: Paint
        get() = _backgroundPaint ?: background
        set(value) {
            _backgroundPaint = value
        }

    lateinit var sceneModel: SceneModel

    private val mouseListener: MouseListener = object : MouseListener {
        override fun mouseClicked(e: MouseEvent) {
            sceneModel.clicked(e.point.componentToScene())
        }

        override fun mousePressed(e: MouseEvent) {
            dragging = sceneModel.findDraggable(e.point.componentToScene())?.let { Dragging(it, e.point) }
        }

        override fun mouseReleased(e: MouseEvent) {
            dragging?.run {
                val delta = (e.point - startPoint)
                if (abs(delta.x) >= 1 || abs(delta.y) >= 1) {
                    sceneModel.releaseDraggable((e.point - startPoint).componentToScene(), sceneObject)
                }
            }
            dragging = null
        }

        override fun mouseEntered(e: MouseEvent) {}

        override fun mouseExited(e: MouseEvent) {}
    }

    private val mouseMotionListener: MouseMotionListener = object : MouseMotionListener {
        override fun mouseDragged(e: MouseEvent) {
            dragging?.run {
                sceneModel.dragging((e.point - startPoint).componentToScene(), sceneObject)
            }
        }

        override fun mouseMoved(e: MouseEvent) {}
    }

    init {
        addMouseListener(mouseListener)
        addMouseMotionListener(mouseMotionListener)
    }

    override fun paintComponent(g: Graphics) {
        require(g is Graphics2D)
        if (isOpaque) {
            g.paint = backgroundPaint
            g.fillRect(0, 0, width, height)
        }
        g.scale(1.0 / sceneModel.width, 1.0 / sceneModel.width)
        sceneModel.drawables.forEach {
            it.draw(g)
        }
    }

    private fun Point2D.componentToScene(): Point2D = point(x / width * sceneModel.width, y / height * sceneModel.height)
    private fun Point2D.sceneToComponent(): Point =
        point((x / sceneModel.width * width).roundToInt(), (y / sceneModel.height * height).roundToInt())
}

