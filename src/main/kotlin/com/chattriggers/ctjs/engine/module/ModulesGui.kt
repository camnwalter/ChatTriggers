package com.chattriggers.ctjs.engine.module

import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.wrappers.Player

//#if MC==10809
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
//#else
//$$ import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//$$ import net.minecraft.client.gui.screen.Screen
//#endif

//#if MC==10809
object ModulesGui : GuiScreen() {
//#else
//$$ object ModulesGui : Screen(MCStringTextComponent("")) {
//#endif
    private val window = object {
        var title = Text("Modules").setScale(2f).setShadow(true)
        var exit = Text(ChatLib.addColor("&cx")).setScale(2f)
        var height = 0f
        var scroll = 0f
    }

    //#if MC==10809
    override fun doesGuiPauseGame() = false

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        GlStateManager.pushMatrix()
    //#else
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //$$     super.render(matrixStack, mouseX, mouseY, partialTicks)
    //$$     matrixStack.push()
    //#endif

        val middle = Renderer.screen.getWidth() / 2f
        var width = Renderer.screen.getWidth() - 100f
        if (width > 500) width = 500f

        Renderer.drawRect(
            0x50000000,
            0f,
            0f,
            Renderer.screen.getWidth().toFloat(),
            Renderer.screen.getHeight().toFloat()
        )

        if (-window.scroll > window.height - Renderer.screen.getHeight() + 20)
            window.scroll = -window.height + Renderer.screen.getHeight() - 20
        if (-window.scroll < 0) window.scroll = 0f

        if (-window.scroll > 0) {
            Renderer.drawRect(0xaa000000, Renderer.screen.getWidth() - 20f, Renderer.screen.getHeight() - 20f, 20f, 20f)
            Renderer.drawString("^", Renderer.screen.getWidth() - 12f, Renderer.screen.getHeight() - 12f)
        }

        Renderer.drawRect(0x50000000, middle - width / 2f, window.scroll + 95f, width, window.height - 90)

        Renderer.drawRect(0xaa000000, middle - width / 2f, window.scroll + 95f, width, 25f)
        window.title.draw(middle - width / 2f + 5, window.scroll + 100f)
        window.exit.setString(ChatLib.addColor("&cx")).draw(middle + width / 2f - 17, window.scroll + 99f)

        window.height = 125f
        ModuleManager.cachedModules.forEach {
            window.height += it.draw(middle - width / 2f, window.scroll + window.height, width)
        }

        //#if MC==10809
        GlStateManager.popMatrix()
        //#else
        //$$ matrixStack.pop()
        //#endif
    }

    //#if MC==10809
    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        super.mouseClicked(mouseX, mouseY, button)
    //#else
    //$$ override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
    //$$     super.mouseClicked(mouseX, mouseY, button)
    //#endif

        val returnValue =
        //#if MC==10809
        Unit
        //#else
        //$$ true
        //#endif

        var width = Renderer.screen.getWidth() - 100f
        if (width > 500) width = 500f

        if (mouseX > Renderer.screen.getWidth() - 20 && mouseY > Renderer.screen.getHeight() - 20) {
            window.scroll = 0f
            return returnValue
        }

        if (mouseX > Renderer.screen.getWidth() / 2f + width / 2f - 25 && mouseX < Renderer.screen.getWidth() / 2f + width / 2f
            && mouseY > window.scroll + 95 && mouseY < window.scroll + 120
        ) {
            Player.getPlayer()?.closeScreen()
            return returnValue
        }

        ModuleManager.cachedModules.toList().forEach {
            it.click(mouseX.toInt(), mouseY.toInt(), width)
        }

        return returnValue
    }

    //#if MC==10809
    override fun handleMouseInput() {
        super.handleMouseInput()
        val i = Mouse.getEventDWheel()
        window.scroll += i / 10
    }
    //#else
    //$$ override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
    //$$     super.mouseScrolled(mouseX, mouseY, delta)
    //$$     window.scroll += delta.toFloat() / 10f
    //$$     return true
    //$$ }
    //#endif
}
