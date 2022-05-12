package com.chattriggers.ctjs.browser.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick

class Modal(
    highlightedBlock: HighlightedBlock = HighlightedBlock(
        backgroundColor = VigilancePalette.getDarkBackground(),
        highlightColor = VigilancePalette.getDarkHighlight(),
        highlightHoverColor = VigilancePalette.getAccent()
    ),
    backgroundAlpha: Int = 150,
) : UIBlock(VigilancePalette.getModalBackground().withAlpha(backgroundAlpha)) {
    private val container by highlightedBlock.constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }

    init {
        constrain {
            x = 0.percentOfWindow()
            y = 0.percentOfWindow()
            width = 100.percentOfWindow()
            height = 100.percentOfWindow()
        }

        super.addChild(container)

        container.constrainBasedOnChildren()
        container.contentContainer.constrain {
            width = ChildBasedSizeConstraint() + 20.pixels()
            height = ChildBasedSizeConstraint() + 20.pixels()
        }

        onLeftClick { event ->
            if (event.target == this)
                fadeOut()
        }

        onKeyType { _, keyCode ->
            if (keyCode == 1) // Escape
                fadeOut()
        }
    }

    override fun addChild(component: UIComponent) = apply {
        component childOf container
    }

    override fun afterInitialization() {
        setFloating(true)
    }

    fun fadeIn(callback: (() -> Unit)? = null) {
        unhide()
        setFloating(true)
        callback?.invoke()
    }

    fun fadeOut(callback: (() -> Unit)? = null) {
        hide(instantly = true)
        setFloating(false)
        callback?.invoke()
    }
}
