package com.stochastictinkr.svg

import com.stochastictinkr.skywing.geom.*
import com.stochastictinkr.skywing.rendering.*
import org.apache.batik.anim.dom.*
import org.apache.batik.bridge.*
import org.apache.batik.ext.awt.image.GraphicsUtil.*
import org.apache.batik.gvt.*
import java.awt.*
import java.awt.image.*
import java.io.*

object BatikSvgLoader : SvgLoader {
    override fun loadSvg(inputStream: InputStream): SvgPainter {
        val userAgent = UserAgentAdapter()
        return BatikSvgPainter(
            GVTBuilder()
                .build(
                    BridgeContext(userAgent),
                    SAXSVGDocumentFactory(userAgent.xmlParserClassName, false)
                        .createSVGDocument(
                            "document",
                            inputStream
                        )
                ).root!!
        )
    }


    private class BatikSvgPainter(val node: GraphicsNode) : SvgPainter {
        override val bounds get() = node.bounds
        override fun drawTo(image: BufferedImage) {
            val size = node.bounds.size
            val g = createGraphics(image)
            try {
                g.color = Color(0, 0, 0, 0)
                g.fillRect(0, 0, image.width, image.height)
                g.scale(image.width / size.width, image.height / size.height)
                g.hints {
                    renderingQuality()
                    antialiasingOn()
                }
                node.paint(g)
            } finally {
                g.dispose()
            }
        }
    }

}