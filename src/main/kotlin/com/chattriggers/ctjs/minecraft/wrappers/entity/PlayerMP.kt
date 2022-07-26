package com.chattriggers.ctjs.minecraft.wrappers.entity

import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.utils.kotlin.asMixin
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer

//#if MC<=11202
import com.chattriggers.ctjs.mixins.entity.EntityPlayerAccessor
//#elseif MC>=11701
//$$import com.chattriggers.ctjs.mixins.GameProfileAccessor
//#endif

class PlayerMP(override val entity: EntityPlayer) : EntityLivingBase(entity) {
    fun isSpectator() = entity.isSpectator

    fun getPing(): Int {
        //#if MC<=11202
        return getPlayerInfo()?.responseTime ?: -1
        //#else
        //$$ return getPlayerInfo()?.latency ?: -1
        //#endif
    }

    fun getTeam(): Team? {
        //#if MC<=11202
        return getPlayerInfo()?.playerTeam?.let(::Team)
        //#else
        //$$ return getPlayerInfo()?.team?.let(::Team)
        //#endif
    }

    /**
     * Gets the display name for this player,
     * i.e. the name shown in tab list and in the player's nametag.
     * @return the display name
     */
    fun getDisplayName(): UTextComponent {
        return UTextComponent(getPlayerName(getPlayerInfo()))
    }

    fun setTabDisplayName(textComponent: UTextComponent) {
        //#if MC<=11202
        getPlayerInfo()?.displayName = textComponent.component
        //#else
        //$$ getPlayerInfo()?.tabListDisplayName = textComponent.component
        //#endif
    }

    /**
     * Sets the name for this player shown above their head,
     * in their name tag
     *
     * @param textComponent the new name to display
     */
    fun setNametagName(textComponent: UTextComponent) {
        //#if MC<=11202
        entity.asMixin<EntityPlayerAccessor>().displayName = textComponent.formattedText
        //#else
        //$$ entity.gameProfile.asMixin<GameProfileAccessor>().setName(textComponent.component.string)
        //#endif
    }

    // TODO(BREAKING) Removed unneeded player param
    /**
     * Draws the player in the GUI
     */
    @JvmOverloads
    fun draw(
        x: Int,
        y: Int,
        rotate: Boolean = false,
        showNametag: Boolean = false,
        showArmor: Boolean = true,
        showCape: Boolean = true,
        showHeldItem: Boolean = true,
        showArrows: Boolean = true,
        //#if MC>=11701
        //$$ showParrot: Boolean = true,
        //$$ showSpinAttack: Boolean = true,
        //$$ showBeeStinger: Boolean = true,
        //#endif
    ) = apply {
        Renderer.drawPlayer(
            this,
            x,
            y,
            rotate,
            showNametag,
            showArmor,
            showCape,
            showHeldItem,
            showArrows,
            //#if MC>=11701
            //$$ showParrot,
            //$$ showSpinAttack,
            //$$ showBeeStinger,
            //#endif
        )
    }

    private fun getPlayerName(info: NetworkPlayerInfo?): String {
        if (info == null)
            return ""

        //#if MC<=11202
        val name = info.displayName
        //#else
        //$$ val name = info.tabListDisplayName
        //#endif

        if (name != null)
            return UTextComponent(name).formattedText

        //#if MC<=11202
        return net.minecraft.scoreboard.ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile?.name) ?: ""
        //#elseif MC>=11701
        //#if FORGE
        //$$ return net.minecraft.world.scores.PlayerTeam.formatNameForTeam(info.team, info.profile?.name?.let(::UTextComponent))
        //$$     .let { UTextComponent(it).formattedText }
        //#else
        //$$ return net.minecraft.scoreboard.Team.decorateName(info.scoreboardTeam, info.profile?.name?.let(::UTextComponent))
        //$$     .let { UTextComponent(it).formattedText }
        //#endif
        //#endif
    }

    private fun getPlayerInfo(): NetworkPlayerInfo? = Client.getConnection()?.getPlayerInfo(getUUID())

    override fun toString(): String {
        return "PlayerMP{name=" + getName() +
                ", ping=" + getPing() +
                ", entityLivingBase=" + super.toString() +
                "}"
    }

    override fun getName(): String = UTextComponent(entity.name).formattedText
}
