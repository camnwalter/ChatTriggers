package com.chattriggers.ctjs.browser

import com.chattriggers.ctjs.browser.pages.AccountPage
import com.chattriggers.ctjs.browser.pages.LoginPage
import com.chattriggers.ctjs.browser.pages.AllModulesPage
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.state
import gg.essential.vigilance.gui.VigilancePalette

object ModuleBrowser : WindowScreen(restoreCurrentGuiOnClose = true, newGuiScale = 2) {
    var isLoggedIn by state(false)
    var username = BasicState<String?>(null)
    var id = BasicState<Int?>(null)
    private var displayedPage: Page = Page.Modules

    init {
        UIBlock(VigilancePalette.getBackground()).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf window
    }

    private val content by UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 85.percent()
        height = 85.percent()
    } childOf window

    init {
        UIBlock(VigilancePalette.getDivider()).constrain {
            width = 1.pixel()
            height = 100.percent()
        } childOf content
    }

    private val panel by Panel.constrain {
        x = NearestSiblingConstraint()
    } childOf content

    init {
        UIBlock(VigilancePalette.getDivider()).constrain {
            x = NearestSiblingConstraint()
            width = 1.pixel()
            height = 100.percent()
        } childOf content
    }

    private val main by UIContainer().constrain {
        x = NearestSiblingConstraint()
        width = FillConstraint() - 1.pixel()
        height = 100.percent()
    } effect ScissorEffect() childOf content

    init {
        UIBlock(VigilancePalette.getDivider()).constrain {
            x = NearestSiblingConstraint()
            width = 1.pixel()
            height = 100.percent()
        } childOf content

        displayedPage.component childOf main
    }

    fun showPage(page: Page) {
        val oldComponent = main.children[0]
        val newComponent = page.component

        if (oldComponent == newComponent)
            return

        val transitionDown = displayedPage.index < page.index
        displayedPage = page

        main.insertChildAfter(newComponent, oldComponent)

        newComponent.constrain {
            y = NearestSiblingConstraint(0f, alignOpposite = !transitionDown)
        }

        oldComponent.animate {
            setYAnimation(
                Animations.OUT_EXP,
                0.5f,
                (oldComponent.getHeight() * if (transitionDown) -1 else 1).pixels(),
            )

            onComplete {
                newComponent.constrain {
                    y = 0.pixels()
                }
                main.removeChild(oldComponent)
            }
        }
    }

    init {
        Inspector(window) childOf window
    }

    sealed class Page(val index: Int, open val component: UIContainer) {
        object Modules : Page(0, AllModulesPage)

        object Account : Page(1, UIContainer()) {
            override val component: UIContainer
                get() = if (isLoggedIn) AccountPage else LoginPage
        }
    }
}
