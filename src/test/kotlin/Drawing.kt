import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import javax.swing.AbstractAction
import javax.swing.ActionMap
import javax.swing.InputMap
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.KeyStroke
import javax.swing.UIManager
import javax.swing.text.JTextComponent
import javax.swing.undo.UndoManager
import kotlin.math.min
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.util.isError
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate


@KotlinScript()
open class Drawing {
}

object DrawingConfig : ScriptCompilationConfiguration()

fun main() {
    EventQueue.invokeLater {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        with(JFrame("Drawing")) {
            val log = JTextArea()
            log.lineWrap = true
            log.isEditable = false

            val sourceCode = JTextArea()
            sourceCode.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            sourceCode.text = """
        moveTo(+0.0, -0.0)
        lineTo(-.02, 0.15)
        quadTo(0.0, 0.48, -0.10, 0.48)
        lineTo(-.11, .5)
        lineTo(+.11, .5)
        lineTo(+.10, .48)
        quadTo(0.0, 0.48, +0.02, +0.15)
        lineTo(0.0, 0.0)
        closePath()
                    """.trimIndent()
            sourceCode.makeUndoable()
            var shape: Shape? = runScript(sourceCode.text) { log.append("$it\n") }
            var repainter: () -> Unit
            var color = Color.RED
            add(
                JSplitPane(JSplitPane.VERTICAL_SPLIT).apply {
                    bottomComponent = JScrollPane(log)
                    topComponent = JSplitPane().apply {
                        leftComponent = object : JComponent() {
                            init {
                                preferredSize = Dimension(400, 400)
                                isOpaque = true
                                repainter = ::repaint
                                addMouseMotionListener(object : MouseMotionListener {
                                    override fun mouseDragged(e: MouseEvent?) {
                                    }

                                    override fun mouseMoved(e: MouseEvent) {
                                        val scale = min(width, height).toDouble()
                                        toolTipText = String.format("%1.2f, %3.2f", e.x / scale - .5, e.y / scale - .5)
                                    }
                                })
                            }

                            override fun paintComponent(g: Graphics?) {
                                with(g as Graphics2D) {
                                    g.setRenderingHint(
                                        RenderingHints.KEY_RENDERING,
                                        RenderingHints.VALUE_RENDER_QUALITY
                                    )
                                    g.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON
                                    )
                                    paint = Color.WHITE
                                    fillRect(0, 0, width, height)
                                    val scale = min(width, height).toDouble()
                                    scale(scale, scale)
                                    translate(.5, .5)
                                    stroke = BasicStroke(0f)
                                    paint = Color.GRAY
                                    draw(Rectangle2D.Double(-.5, -.5, 1.0, 1.0))
                                    paint = color
                                    fill(shape)
                                    paint = Color.GREEN
                                    draw(Ellipse2D.Double().apply {
                                        this.setFrameFromCenter(0.0, 0.0, .01, .01)
                                    })
                                }
                            }
                        }
                        rightComponent = JPanel(BorderLayout()).apply {
                            add(
                                JComboBox(arrayOf(Color.RED, Color.BLACK)).apply {
                                    addActionListener {
                                        color = this.selectedItem as Color
                                        repainter()
                                    }
                                },
                                BorderLayout.NORTH
                            )
                            add(JScrollPane(sourceCode))
                            add(JButton("Run").apply {
                                action = object : AbstractAction("Run") {
                                    override fun actionPerformed(e: ActionEvent?) {
                                        runScript(sourceCode.text) { log.append("$it\n") }?.let {
                                            shape = it
                                            repainter()
                                        }
                                    }
                                }
                            }, BorderLayout.SOUTH)
                        }
                    }
                }
            )
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            pack()
            setLocationRelativeTo(null)
            isVisible = true
        }
    }
}

fun runScript(text: String, log: (String) -> Unit): Shape? {
    val shape = Path2D.Double()
    val results = BasicJvmScriptingHost().eval(
        StringScriptSource(text),
        createJvmCompilationConfigurationFromTemplate<Drawing> {
            jvm {
                dependenciesFromCurrentContext(wholeClasspath = true)
            }
            this.implicitReceivers.put(listOf(KotlinType(Path2D::class)))
        },
        ScriptEvaluationConfiguration {
            this.implicitReceivers.put(listOf(shape))
        }
    )
    results.reports.forEach {
        log(it.message)
    }
    return if (results.isError()) null else shape
}

fun JTextComponent.makeUndoable() {
    val im: InputMap = getInputMap(JComponent.WHEN_FOCUSED)
    val am: ActionMap = actionMap
    val undoManager = UndoManager()
    document.addUndoableEditListener(undoManager)

    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, toolkit.menuShortcutKeyMaskEx), "Undo")
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, toolkit.menuShortcutKeyMaskEx or InputEvent.SHIFT_DOWN_MASK), "Redo")

    am.put("Undo", object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            if (undoManager.canUndo()) {
                undoManager.undo()
            }
        }
    })
    am.put("Redo", object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            if (undoManager.canRedo()) {
                undoManager.redo()
            }
        }
    })
}