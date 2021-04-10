package com.chattriggers.ctjs.utils.config

import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.utils.kotlin.MCButton
import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent

//#if MC!=10809
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//$$ import net.minecraft.client.gui.widget.Widget
//$$ import net.minecraft.client.gui.widget.button.Button

//#endif

abstract class ConfigOption(
    val name: String,
    var x: Int,
    var y: Int
) {
    var hidden: Boolean = false

    val text = Text("\u21BA", (Renderer.screen.getWidth() / 2 - 100 + x + 189).toFloat(), (y - 4).toFloat())
        .setScale(2f)
        .setColor(-0x1)
        .setShadow(true)

    //#if MC==10809
    protected val resetButton = MCButton(
        0,
        Renderer.screen.getWidth() / 2 - 100 + x + 185,
        y - 2,
        14, 12, ""
    )
    //#else
    //$$ protected val resetButton = MCButton(
    //$$     Renderer.screen.getWidth() / 2 - 100 + x + 185,
    //$$     y - 2,
    //$$     200,
    //$$     20,
    //$$     MCStringTextComponent(""),
    //$$     Button.IPressable { onReset() }
    //$$ )
    //#endif

    //#if MC==10809
    open fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        resetButton.xPosition = Renderer.screen.getWidth() / 2 - 100 + x + 185
        resetButton.drawButton(Client.getMinecraft(), mouseX, mouseY)
    //#else
    //$$ open fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //$$     resetButton.x = Renderer.screen.getWidth() / 2 - 100 + x + 185
    //$$     resetButton.render(matrixStack, mouseX, mouseY, partialTicks)
    //#endif
        //#endif

        text.setX((Renderer.screen.getWidth() / 2 - 100 + x + 189).toFloat()).draw()
    }

    //#if MC==10809
    abstract fun mouseClicked(mouseX: Int, mouseY: Int)
    //#else
    //$$ abstract fun onReset()
    //$$
    //$$ open fun getWidgets(): List<Widget> = listOf(resetButton)
    //#endif

    open fun mouseReleased() {}

    //#if MC==10809
    open fun keyTyped(typedChar: Char, keyCode: Int) {}
    //#else
    //$$ open fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) {}
    //$$ open fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {}
    //#endif
}
