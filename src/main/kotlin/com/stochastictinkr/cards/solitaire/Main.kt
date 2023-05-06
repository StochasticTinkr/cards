package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.CardBacks
import com.stochastictinkr.skywing.initSkywing
import com.stochastictinkr.skywing.swing.accelerator
import com.stochastictinkr.skywing.swing.action
import com.stochastictinkr.skywing.swing.actionCommand
import com.stochastictinkr.skywing.swing.addMenu
import com.stochastictinkr.skywing.swing.menuBar
import java.awt.EventQueue.invokeLater
import java.awt.Frame
import java.awt.event.KeyEvent
import javax.swing.Action
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
            menuBar {
                addMenu {
                    text = "Game"
                    add(newGame)
                    addSeparator()
                    add(quit)
                }
                addMenu {
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

private fun SolitaireComponent.addAction(
    undo: Action,
) {
    inputMap.put(undo.accelerator, undo.actionCommand)
    actionMap.put(undo.actionCommand, undo)
}

private fun keyStroke(stroke: String): KeyStroke? = KeyStroke.getKeyStroke(stroke)