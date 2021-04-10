package com.chattriggers.ctjs.utils.config

import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.minecraft.libs.renderer.Rectangle
import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.utils.kotlin.MCButton
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty

//#if MC!=10809
//$$ import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//$$ import net.minecraft.client.gui.widget.Widget
//$$ import net.minecraft.client.gui.widget.button.Button
//#endif

class ConfigBoolean(
    private val prop: KMutableProperty<Boolean>,
    name: String = "",
    x: Int = 0,
    y: Int = 0
) : ConfigOption(name, x, y) {
    private var value: Boolean by Delegates.observable(prop.getter.call(Config)) { _, _, new ->
        prop.setter.call(Config, new)
    }
    private val initial: Boolean = value

    //#if MC==10809
    private var button = MCButton(
        0,
        Renderer.screen.getWidth() / 2 - 100 + x,
        y + 15,
        stringValue
    )
    //#else
    //$$ private var button = MCButton(
    //$$     Renderer.screen.getWidth() / 2 - 100 + x,
    //$$     y + 15,
    //$$     200,
    //$$     20,
    //$$     MCStringTextComponent(stringValue),
    //$$     Button.IPressable {
    //$$         if (!hidden) {
    //$$             value = !value
    //$$             it.playDownSound(Client.getMinecraft().soundHandler)
    //$$             it.message = MCStringTextComponent(stringValue)
    //$$         }
    //$$     }
    //$$ )

    private val stringValue: String
        get() = if (value) ChatLib.addColor("&aTrue") else ChatLib.addColor("&cFalse")

    //#if MC==10809
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
    //#else
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //#endif
        if (hidden)
            return

        val middle = Renderer.screen.getWidth() / 2

        Rectangle(-0x80000000, (middle - 105 + x).toFloat(), (y - 5).toFloat(), 210f, 45f)
            .setShadow(-0x30000000, 3f, 3f)
            .draw()

        Text(name, (middle - 100 + x).toFloat(), y.toFloat()).draw()

        //#if MC<=10809
        button.xPosition = middle - 100 + x
        button.drawButton(Client.getMinecraft(), mouseX, mouseY)
        super.draw(mouseX, mouseY, partialTicks)
        //#else
        //$$ button.x = middle - 100 + x;
        //$$ button.render(matrixStack, mouseX, mouseY, partialTicks)
        //$$ super.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif
    }

    //#if MC==10809
    override fun mouseClicked(mouseX: Int, mouseY: Int) {
        if (hidden) return

        if (button.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            value = !value
            button.playPressSound(Client.getMinecraft().soundHandler)
        }

        if (resetButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            value = initial
            resetButton.playPressSound(Client.getMinecraft().soundHandler)
        }

        button.displayString = stringValue
    }
    //#else
    //$$ override fun onReset() {
    //$$     if (hidden)
    //$$         return
    //$$
    //$$     value = initial
    //$$     resetButton.message = MCStringTextComponent(stringValue)
    //$$ }
    //$$
    //$$ override fun getWidgets(): List<Widget> = super.getWidgets() + listOf(button)
    //#endif
}
