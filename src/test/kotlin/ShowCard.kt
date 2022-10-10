import com.stochastictinkr.cards.standardDeck
import com.stochastictinkr.skywing.awt.geom.size
import com.stochastictinkr.skywing.initSkywing
import com.stochastictinkr.skywing.rendering.geom.component1
import com.stochastictinkr.skywing.rendering.geom.component2
import com.stochastictinkr.svg.drawTo
import com.stochastictinkr.svg.loadSvgGraphicsNode
import java.awt.EventQueue.invokeLater
import java.awt.GridLayout
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

fun main() {
    val deck = standardDeck(true)
    val back = {}.javaClass.classLoader.getResourceAsStream("cards/backs/blue.svg")!!
    val backNode = loadSvgGraphicsNode("card back", back)
    initSkywing()
    invokeLater {
        JFrame().apply {
            add(JScrollPane(JPanel(GridLayout(0, 13)).apply {
                deck.cards.forEach { card ->
                    val (w, h) = card.image.bounds.size
                    val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
                    card.image.drawTo(image)
                    add(JLabel(ImageIcon(image)))
                }
                val (w, h) = backNode.bounds.size
                val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
                backNode.drawTo(image)
                add(JLabel(ImageIcon(image)))

            }))
            pack()
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            isLocationByPlatform = true
            isVisible = true
        }
    }


}