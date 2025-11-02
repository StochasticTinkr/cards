package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.skywing.initSkywing
import com.stochastictinkr.skywing.components.events.action
import com.stochastictinkr.skywing.components.addAction
import com.stochastictinkr.skywing.components.createMenuBar
import com.stochastictinkr.skywing.components.menu
import java.awt.EventQueue.invokeLater
import java.awt.Frame
import java.awt.event.KeyEvent
import javax.swing.ButtonGroup
import javax.swing.JFrame
import javax.swing.JRadioButtonMenuItem
import javax.swing.KeyStroke
import kotlin.system.exitProcess

fun main() {
    initSkywing("Solitaire")
    invokeLater {
        val jFrame = JFrame()
        with(jFrame.rootPane) {
            putClientProperty("apple.awt.fullscreenable", true)
            putClientProperty("apple.awt.fullWindowContent", true)
            putClientProperty("apple.awt.transparentTitleBar", true)
        }
        val solitaireGame = SolitaireGame()
        solitaireGame.newGame()

        val solitaireComponent = SolitaireComponent(solitaireGame)
        val undo = action(
            name = "Undo",
            mnemonicKeyCode = KeyEvent.VK_U,
            accelerator = keyStroke("meta Z"),
            actionCommand = "Undo"
        ) { solitaireGame.undo() }

        val redo = action(
            name = "Redo",
            mnemonicKeyCode = KeyEvent.VK_R,
            accelerator = keyStroke("shift meta Z"),
            actionCommand = "Redo"
        ) { solitaireGame.redo() }

        solitaireComponent.addAction(undo)
        solitaireComponent.addAction(redo)
        val newGame = action(
            name = "New Game",
            accelerator = keyStroke("meta N"),
            mnemonicKeyCode = KeyEvent.VK_N,
        ) { solitaireGame.newGame() }
        val quit = action(
            name = "Quit",
            mnemonicKeyCode = KeyEvent.VK_Q,
            accelerator = keyStroke("meta Q")
        ) { exitProcess(0) }
        val chooseDeckActions = CardBacks.values().map { cardBack ->
            action(
                name = cardBack.displayName,
                isSelected = cardBack == solitaireComponent.cardBack
            ) { solitaireComponent.cardBack = cardBack }
        }


        with(jFrame) {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            createMenuBar {
                menu {
                    text = "Game"
                    add(newGame)
                    addSeparator()
                    add(action(name = "Settings…", mnemonicKeyCode = KeyEvent.VK_S) {
                        OptionsDialog(this@with, solitaireComponent).isVisible = true
                    })
                    addSeparator()
                    add(quit)
                }
                menu {
                    text = "Deck"
                    val group = ButtonGroup()
                    chooseDeckActions.map { action -> JRadioButtonMenuItem(action) }
                        .forEach {
                            add(it)
                            group.add(it)
                        }
                }
            }
            add(solitaireComponent)
            extendedState = Frame.MAXIMIZED_BOTH
            isVisible = true
        }
    }
}

private fun keyStroke(stroke: String): KeyStroke? = KeyStroke.getKeyStroke(stroke)