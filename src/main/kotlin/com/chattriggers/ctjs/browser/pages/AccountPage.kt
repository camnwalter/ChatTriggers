package com.chattriggers.ctjs.browser.pages

import com.chattriggers.ctjs.browser.BrowserEntry
import com.chattriggers.ctjs.browser.ModuleBrowser
import com.chattriggers.ctjs.browser.NearestSiblingConstraint
import com.chattriggers.ctjs.browser.WebsiteAPI
import com.chattriggers.ctjs.browser.components.ButtonComponent
import com.chattriggers.ctjs.minecraft.wrappers.Player
import com.mojang.authlib.GameProfile
import gg.essential.api.EssentialAPI
import gg.essential.api.gui.buildEmulatedPlayer
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick
import java.util.*
import kotlin.concurrent.thread

object AccountPage : UIContainer() {
    private var moduleOffset = 0
    private var maxModules = 10

    private val container by UIContainer().constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf this

    private val header by UIContainer().constrain {
        width = 100.percent()
        height = 30.pixels()
    } childOf container

    private val username by UIText(ModuleBrowser.username.get() ?: "").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        textScale = 2.pixels()
        color = VigilancePalette.getAccent().toConstraint()
    } childOf header

    private val logoutButton by ButtonComponent("Logout").constrain {
        x = 10.pixels(alignOpposite = true)
        y = CenterConstraint()
    }.onLeftClick {
        moduleOffset = 0
        maxModules = 10
        moduleContent.clearChildren()
        WebsiteAPI.logout()
    } childOf header

    init {
        UIBlock(VigilancePalette.getDivider()).constrain {
            x = 15.pixels()
            y = NearestSiblingConstraint(15f)
            width = 100.percent() - 30.pixels()
            height = 1.pixel()
        } childOf container
    }

    private val moduleContentContainer by UIContainer().constrain {
        x = 45.pixels()
        y = NearestSiblingConstraint(15f)
        width = 100.percent() - 90.pixels()
        height = basicHeightConstraint { container.getBottom() - it.getTop() }
    } childOf container

    private val content by UIContainer().constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf moduleContentContainer

    private val lhs by UIContainer().constrain {
        width = 40.percent()
        height = 100.percent()
    } childOf content

    private val rhs by UIContainer().constrain {
        x = NearestSiblingConstraint()
        width = 60.percent()
        height = 100.percent()
    } childOf content

    private val moduleContent by ScrollComponent("Loading...").constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf rhs

    private val userModel by EssentialAPI.getEssentialComponentFactory().buildEmulatedPlayer {
        profile = GameProfile(UUID.fromString(Player.getUUID()), Player.getName())
    }.constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 100.pixels()
        height = 300.pixels()
    } childOf lhs

    init {
        constrain {
            width = 100.percent()
            height = 100.percent()
        }

        ModuleBrowser.username.onSetValue {
            if (it != null)
                username.setText(it)
        }

        ModuleBrowser.id.onSetValue {
            if (it != null) {
                loadModules(it)
            }
        }

        moduleContent.addScrollAdjustEvent(isHorizontal = false) { scrollPercentage, _ ->
            if (scrollPercentage >= 0.8) {
                moduleOffset++
                if (moduleOffset * 10 >= maxModules)
                    return@addScrollAdjustEvent

                ModuleBrowser.id.get()?.let(::loadModules)
            }
        }
    }

    private fun loadModules(id: Int) {
        thread {
            val (meta, modules) = WebsiteAPI.getUserModules(id, moduleOffset) ?: return@thread
            maxModules = meta.total

            Window.enqueueRenderOperation {
                modules.forEach {
                    // TODO: Add module preview
                    val entry = BrowserEntry(it) childOf moduleContent
                }
            }
        }
    }
}
