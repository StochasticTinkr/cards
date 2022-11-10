package com.stochastictinkr.scene

import java.awt.geom.Point2D

interface SceneModel {
    fun findDraggable(point: Point2D): SceneObject?
    fun clicked(point: Point2D)
    fun releaseDraggable(delta: Point2D, sceneObject: SceneObject)
    fun dragging(delta: Point2D, sceneObject: SceneObject)

    val width: Double
    val height: Double
    val drawables: Iterable<Drawable>
}
