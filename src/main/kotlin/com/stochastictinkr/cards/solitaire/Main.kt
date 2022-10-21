package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.skywing.initSkywing
import com.stochastictinkr.skywing.swing.action
import com.stochastictinkr.skywing.swing.addItem
import com.stochastictinkr.skywing.swing.addMenu
import com.stochastictinkr.skywing.swing.menuBar
import java.awt.EventQueue.invokeLater
import java.awt.Frame
import javax.swing.JFrame
import kotlin.system.exitProcess

fun main() {
    initSkywing("Solitaire")
    invokeLater {
        val jFrame = JFrame()
        with (jFrame.rootPane) {
            putClientProperty("apple.awt.fullscreenable", true)
            putClientProperty("apple.awt.fullWindowContent", true)
            putClientProperty("apple.awt.transparentTitleBar", true)
        }
        val solitaireModel = SolitaireModel()
        solitaireModel.newGame()

        val solitaireComponent = SolitaireComponent(solitaireModel)
        val newGame = action(name = "Name Game", actionCommand = "New Game") {
            solitaireModel.newGame()
            solitaireComponent.repaint()
        }
        val quit = action(name = "Quit") {
            exitProcess(0)
        }
        with(jFrame) {
            menuBar {
                addMenu {
                    text = "Game"
                    addItem(newGame)
                    addSeparator()
                    addItem(quit)
                }
            }
            add(solitaireComponent)
            extendedState = Frame.MAXIMIZED_BOTH
            isVisible = true
        }
    }
}