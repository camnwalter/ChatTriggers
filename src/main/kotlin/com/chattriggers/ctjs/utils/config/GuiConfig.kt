package com.chattriggers.ctjs.utils.config

import com.chattriggers.ctjs.CTJS
import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent
import net.minecraft.client.Minecraft
import java.io.IOException
import java.util.*
import kotlin.reflect.full.declaredMemberProperties

//#if MC==10809
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
//#else
//$$ import net.minecraft.client.gui.screen.Screen
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//#endif

//#if MC==10809
class GuiConfig : GuiScreen() {
//#else
//$$ class GuiConfig : Screen(MCStringTextComponent("")) {
//#endif
    private val configOptions: ArrayList<ConfigOption> = ArrayList()

    init {
        Config::class.declaredMemberProperties.forEach { prop ->
            prop.annotations.firstOrNull { ann ->
                ann.annotationClass == ConfigOpt::class
            }?.let { ann ->
                val opt = ann as ConfigOpt
                configOptions.add(
                    opt.type.constructors.first().call(
                        prop,
                        opt.name,
                        opt.x,
                        opt.y
                    ) as ConfigOption
                )
            }
        }
    }

    //#if MC==10809
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GlStateManager.pushMatrix()
        drawBackground(0)
        for (configOption in configOptions)
            configOption.draw(mouseX, mouseY, partialTicks)

        IconHandler.drawIcons()
        GlStateManager.popMatrix()
    }
    //#else
    //$$ override fun init(minecraft: Minecraft, width: Int, height: Int) {
    //$$     configOptions.flatMap(ConfigOption::getWidgets).forEach { addButton(it) }
    //$$ }
    //$$
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //$$     matrixStack.push()
    //$$     renderBackground(matrixStack)
    //$$     for (configOption in configOptions)
    //$$         configOption.render(matrixStack, mouseX, mouseY, partialTicks)
    //$$
    //$$     IconHandler.drawIcons()
    //$$     matrixStack.pop()
    //$$ }
    //#endif

    //#if MC==10809
    override fun onGuiClosed() {
    //#else
    //$$ override fun onClose() {
    //#endif
        CTJS.saveConfig()
    }

    @Throws(IOException::class)
    //#if MC==10809
    public override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return
        for (configOption in configOptions)
            configOption.mouseClicked(mouseX, mouseY)
    //#else
    //$$ override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
    //$$     //#endif
    //$$     if (button != 0)
    //$$         return true
    //$$
    //$$     IconHandler.clickIcons(mouseX.toInt(), mouseY.toInt())
    //$$     return super.mouseClicked(mouseX, mouseY, button)
    //#endif
    }

    //#if MC==10809
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (configOption in configOptions)
            configOption.mouseReleased()
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        for (configOption in configOptions)
            configOption.keyTyped(typedChar, keyCode)
    }
    //#endif
}
