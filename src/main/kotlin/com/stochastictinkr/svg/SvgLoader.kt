package com.stochastictinkr.svg

import java.io.InputStream

interface SvgLoader {
    fun loadSvg(inputStream: InputStream): SvgPainter
}