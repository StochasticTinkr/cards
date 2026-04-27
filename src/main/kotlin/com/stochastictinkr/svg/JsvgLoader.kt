package com.stochastictinkr.svg

import com.github.weisj.jsvg.*
import com.github.weisj.jsvg.parser.*
import java.awt.*
import java.awt.geom.*
import java.awt.image.*
import java.io.*

class JsvgLoader : SvgLoader {
    override fun loadSvg(inputStream: InputStream): SvgPainter {
        val document = SVGLoader().load(inputStream, null, LoaderContext.createDefault()) ?: error("Can't load image")
        return JsvgPainter(document)
    }

    private class JsvgPainter(
        private val document: SVGDocument,
    ) : SvgPainter {
        override val bounds: Rectangle2D
            get() {
                val size = document.size()
                return Rectangle2D.Double(
                    0.0,
                    0.0,
                    size.width.toDouble(),
                    size.height.toDouble(),
                )
            }

        override fun drawTo(image: BufferedImage) {
            val size = document.size()
            val g = image.createGraphics()

            try {
                val oldComposite = g.composite
                g.composite = AlphaComposite.Clear
                g.fillRect(0, 0, image.width, image.height)
                g.composite = oldComposite

                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)

                g.scale(
                    image.width / size.width.toDouble(),
                    image.height / size.height.toDouble(),
                )

                document.render(null, g)
            } finally {
                g.dispose()
            }
        }
    }
}