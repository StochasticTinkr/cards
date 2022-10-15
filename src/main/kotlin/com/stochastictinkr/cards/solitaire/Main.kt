package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.skywing.initSkywing
import com.stochastictinkr.skywing.swing.action
import com.stochastictinkr.skywing.swing.addItem
import com.stochastictinkr.skywing.swing.addMenu
import com.stochastictinkr.skywing.swing.menuBar
import java.awt.EventQueue.invokeLater
import java.awt.Frame
import javax.swing.JFrame

fun main() {
    initSkywing()
    invokeLater {
        val jFrame = JFrame("Solitaire")
        val solitaireModel = SolitaireModel()
        solitaireModel.newGame()

        val solitaireComponent = SolitaireComponent(solitaireModel)
        val newGame = action(name = "Name Game", actionCommand = "New Game") {
            solitaireModel.newGame()
        }
        val quit = action(name = "quit") {
            jFrame.dispose()
        }
        with(jFrame) {
            menuBar {
                addMenu {
                    name = "Game"
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