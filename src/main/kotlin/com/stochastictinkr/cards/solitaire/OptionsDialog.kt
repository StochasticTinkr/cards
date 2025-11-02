package com.stochastictinkr.cards.solitaire

import com.stochastictinkr.cards.*
import java.awt.*
import javax.swing.*

/**
 * Simple options dialog for the Solitaire app. Allows choosing a card back and toggling
 * animations and auto-finish options.
 */
class OptionsDialog(
    owner: Frame?,
    private val component: SolitaireComponent,
) : JDialog(owner, "Settings", true) {

    private val images = CardImages().apply { cardWidth = 180 }

    private val previewImage: JLabel
    private val okButton: JButton
    private val cancelButton: JButton

    private val cardBackGroup = ButtonGroup()
    private val backButtons = mutableMapOf<CardBacks, JRadioButton>()

    private val animationsCheckbox = JCheckBox("Animations")
    private val autoFinishCheckbox = JCheckBox("Auto-Finish")

    init {
        minimumSize = Dimension(420, 360)
        layout = BorderLayout(12, 12)
        previewImage = JLabel().apply {
            preferredSize = Dimension(images.cardWidth + 6, images.cardHeight + 6)
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
            border = BorderFactory.createEmptyBorder(6, 6, 6, 6)
        }
        okButton = JButton("OK").apply {
            addActionListener {
                applyChanges()
                dispose()
            }
        }
        cancelButton = JButton("Cancel").apply { addActionListener { dispose() } }

        add(buildMainPanel(), BorderLayout.CENTER)
        add(buildButtons(), BorderLayout.SOUTH)

        // Initialize values from component
        backButtons.getValue(component.cardBack).isSelected = true
        animationsCheckbox.isSelected = component.animationsEnabled
        animationsCheckbox.toolTipText = "Enables card animations."
        autoFinishCheckbox.isSelected = component.autoFinishEnabled
        autoFinishCheckbox.toolTipText =
            "Automatically completes applies the finishing moves once all tableau are visible."

        updatePreview(component.cardBack)

        rootPane.defaultButton = okButton
        pack()
        setLocationRelativeTo(owner)
    }

    private fun buildMainPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            weightx = 1.0
            weighty = 0.0
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(8, 12, 8, 12)
        }

        // Card backs section
        val backsPanel = JPanel()
        backsPanel.layout = BoxLayout(backsPanel, BoxLayout.Y_AXIS)
        backsPanel.border = BorderFactory.createTitledBorder("Card Back")
        for (back in CardBacks.entries) {
            val rb = JRadioButton(back.displayName)
            rb.addActionListener { updatePreview(back) }
            cardBackGroup.add(rb)
            backsPanel.add(rb)
            backButtons[back] = rb
        }

        val previewPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Preview")
            add(previewImage, BorderLayout.CENTER)
            preferredSize = Dimension(images.cardWidth + 24, images.cardHeight + 42)
        }

        panel.add(backsPanel, gbc)
        gbc.gridy++
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(previewPanel, gbc)

        // Options
        gbc.gridy++
        gbc.weighty = 0.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        val optionsPanel = JPanel()
        optionsPanel.layout = BoxLayout(optionsPanel, BoxLayout.Y_AXIS)
        optionsPanel.border = BorderFactory.createTitledBorder("Options")
        optionsPanel.add(animationsCheckbox)
        optionsPanel.add(autoFinishCheckbox)
        panel.add(optionsPanel, gbc)

        return panel
    }

    private fun buildButtons(): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder(8, 12, 12, 12)
            add(Box.createHorizontalGlue())
            add(cancelButton)
            add(Box.createRigidArea(Dimension(8, 1)))
            add(okButton)
        }
    }

    private fun updatePreview(back: CardBacks) {
        val img = images[back]
        val icon: Icon = ImageIcon(img as Image)
        previewImage.icon = icon
    }

    private fun applyChanges() {
        // Card back
        val selected = backButtons.entries.firstOrNull { it.value.isSelected }?.key ?: component.cardBack
        component.cardBack = selected
        // Options
        component.animationsEnabled = animationsCheckbox.isSelected
        component.autoFinishEnabled = autoFinishCheckbox.isSelected
        component.repaint()
    }
}
