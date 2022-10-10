import com.stochastictinkr.skywing.awt.add
import com.stochastictinkr.skywing.awt.geom.size
import com.stochastictinkr.skywing.initSkywing
import com.stochastictinkr.skywing.rendering.geom.component1
import com.stochastictinkr.skywing.rendering.geom.component2
import com.stochastictinkr.svg.drawTo
import com.stochastictinkr.svg.loadSvgGraphicsNode
import java.awt.Color
import java.awt.EventQueue.invokeLater
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

fun main() {
    val back = {}.javaClass.classLoader.getResourceAsStream("svg_playing_cards/fronts/clubs_2.svg")!!
    val node = loadSvgGraphicsNode("abstract", back)
    val (w, h) = node.bounds.size
    val image = BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB)
    node.drawTo(image)
    initSkywing()
    invokeLater {
        JFrame().apply {
            add(component = JLabel(ImageIcon(image))) {
                background = Color.BLACK
                isOpaque = true
            }
            pack()
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            isLocationByPlatform = true
            isVisible = true
        }
    }


}