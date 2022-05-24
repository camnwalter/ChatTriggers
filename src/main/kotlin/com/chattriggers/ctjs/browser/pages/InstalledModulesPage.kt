package com.chattriggers.ctjs.browser.pages

import com.chattriggers.ctjs.Reference
import com.chattriggers.ctjs.browser.BrowserEntry
import com.chattriggers.ctjs.browser.NearestSiblingConstraint
import com.chattriggers.ctjs.browser.components.ButtonComponent
import com.chattriggers.ctjs.engine.module.Module
import com.chattriggers.ctjs.engine.module.ModuleManager
import gg.essential.api.EssentialAPI
import gg.essential.api.gui.buildConfirmationModal
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.UMatrixStack
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick

object InstalledModulesPage : UIContainer() {
    private var gotModules = false
    private val modules = ModuleManager.cachedModules.toMutableList()
    private var clickedModule: Module? = null

    private val header by UIContainer().constrain {
        width = 100.percent()
        height = 30.pixels()
    } childOf this

    private val title by UIText("Installed Modules").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        textScale = 2.pixels()
        color = VigilancePalette.getAccent().toConstraint()
    } childOf header

    init {
        UIBlock(VigilancePalette.getDivider()).constrain {
            x = 15.pixels()
            y = NearestSiblingConstraint(15f)
            width = 100.percent() - 30.pixels()
            height = 1.pixel()
        } childOf this
    }

    private val moduleContentContainer by UIContainer().constrain {
        x = 45.pixels()
        y = NearestSiblingConstraint(15f)
        width = 100.percent() - 90.pixels()
        height = basicHeightConstraint { this@InstalledModulesPage.getBottom() - it.getTop() }
    } childOf this

    private val moduleContent by ScrollComponent("Loading...").constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf moduleContentContainer

    private val modal by EssentialAPI.getEssentialComponentFactory().buildConfirmationModal {
        text = "Are you sure you want to uninstall this module?"
        onConfirm = {
            clickedModule?.let { ModuleManager.deleteModule(it.name) }
        }
    } childOf this

    init {
        constrain {
            width = 100.percent()
            height = 100.percent()
        }
        modal.hide()
    }

    override fun draw(matrixStack: UMatrixStack) {
        super.draw(matrixStack)

        if (!Reference.isLoaded) {
            gotModules = false
            moduleContent.clearChildren()
            return
        }

        if (modules.size != ModuleManager.cachedModules.size) {
            gotModules = false
        }
        getModules()
    }

    private fun getModules() {
        if (gotModules) return
        gotModules = true

        moduleContent.clearChildren()
        modules.clear()

        Window.enqueueRenderOperation {
            modules.addAll(ModuleManager.cachedModules)

            modules.forEach { module ->
                val entry = BrowserEntry(module).constrain {
                    height = ChildBasedMaxSizeConstraint()
                } childOf moduleContent

                val removeModule by ButtonComponent("Delete").constrain {
                    x = 10.pixels(alignOpposite = true)
                    y = 10.pixels()
                }.onLeftClick {
                    clickedModule = module
                    modal.unhide()
                } childOf entry
            }
        }
    }
}
