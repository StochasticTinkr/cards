package com.stochastictinkr.svg

import com.stochastictinkr.skywing.awt.geom.size
import com.stochastictinkr.skywing.awt.hints
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgent
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.gvt.GraphicsNode
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream

fun loadSvgGraphicsNode(uri: String? = null, inputStream: InputStream, userAgent: UserAgent = UserAgentAdapter()) =
    GVTBuilder()
        .build(
            BridgeContext(userAgent),
            SAXSVGDocumentFactory(userAgent.xmlParserClassName, false)
                .createSVGDocument(
                    uri,
                    inputStream
                )
        ).root!!


fun GraphicsNode.drawTo(image: BufferedImage) {
    val size = bounds.size
    val g = image.createGraphics()
    try {
        g.color = Color(0, 0, 0, 0)
        g.fillRect(0, 0, image.width, image.height)
        g.scale(image.width / size.width.toDouble(), image.height / size.height.toDouble())
        g.hints {
            renderingQuality()
            antialiasingOn()
        }
        paint(g)
    } finally {
        g.dispose()
    }
}
