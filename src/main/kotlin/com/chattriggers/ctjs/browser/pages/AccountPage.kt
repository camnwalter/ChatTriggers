package com.chattriggers.ctjs.browser.pages

import com.chattriggers.ctjs.browser.ModuleBrowser
import com.chattriggers.ctjs.browser.NearestSiblingConstraint
import com.chattriggers.ctjs.browser.WebsiteAPI
import com.chattriggers.ctjs.browser.components.ButtonComponent
import com.chattriggers.ctjs.minecraft.wrappers.Player
import com.mojang.authlib.GameProfile
import gg.essential.api.EssentialAPI
import gg.essential.api.gui.buildEmulatedPlayer
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick
import java.util.*

object AccountPage : UIContainer() {
    private val header by UIContainer().constrain {
        width = 100.percent()
        height = 30.pixels()
    } childOf this

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
        WebsiteAPI.logout()

        AllModulesPage.page = 0
        ModuleBrowser.username.reset()
        ModuleBrowser.id.reset()
        ModuleBrowser.rank.reset()
        ModuleBrowser.isLoggedIn.reset()
        ModuleBrowser.sessionCookie = null
        LoginPage.clearInputs()
        ModuleBrowser.showPage(ModuleBrowser.Page.Account)
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
        height = basicHeightConstraint { this@AccountPage.getBottom() - it.getTop() }
    } childOf this

    private val lhs by UIContainer().constrain {
        width = 40.percent()
        height = 100.percent()
    } childOf moduleContentContainer

    private val rhs by UIContainer().constrain {
        x = NearestSiblingConstraint()
        width = 60.percent()
        height = 100.percent()
    } childOf moduleContentContainer

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
    }
}
