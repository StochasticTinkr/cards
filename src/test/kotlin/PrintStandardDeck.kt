import com.stochastictinkr.cards.standardDeck
import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.GridLayout
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.UIManager

fun main() {
//    val deck = standardDeck(true)
//    println("Loading images")
//    EventQueue.invokeLater {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
//        with(JFrame("Deck")) {
//            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//            add(
//                JScrollPane(
//                    JPanel().apply {
//                        background = Color.green.darker()
//                        isOpaque = true
//                        layout = GridLayout(0, 13)
//                        deck.cards.forEach {
//                            add(JLabel(ImageIcon(images[it.name])))
//                        }
//                    }
//                ).apply {
//                    preferredSize = Dimension(800, 800)
//                }
//            )
//            pack()
//            isLocationByPlatform = false
//            setLocationRelativeTo(null)
//            isVisible = true
//        }
//    }
}
