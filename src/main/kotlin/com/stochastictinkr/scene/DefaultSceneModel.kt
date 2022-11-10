package com.stochastictinkr.scene

import java.awt.geom.Point2D

class DefaultSceneModel<O : SceneObject> : SceneModel {
    override var width: Double = 100.0
    override val height: Double = 100.0
    override val drawables = mutableListOf<Drawable>()
    private val contents = mutableListOf<O>()

    override fun findDraggable(point: Point2D): SceneObject? {
        return contents
            .asSequence()
            .filter { it.isDraggable && point in it.bounds }
            .maxByOrNull { it.zOrder }
    }

    override fun releaseDraggable(delta: Point2D, sceneObject: SceneObject) {
    }

    override fun dragging(delta: Point2D, sceneObject: SceneObject) {
    }

    override fun clicked(point: Point2D) {
    }

}

