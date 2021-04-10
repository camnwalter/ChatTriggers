package com.chattriggers.ctjs.utils.config

import com.chattriggers.ctjs.minecraft.libs.MathLib
import com.chattriggers.ctjs.minecraft.libs.renderer.Rectangle
import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.utils.kotlin.MCButton
import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent
import java.awt.Color
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty

//#if MC!=10809
//$$ import net.minecraft.client.gui.widget.button.Button
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//$$ import net.minecraft.client.gui.widget.Widget
//#endif

open class ConfigColor(
    private val prop: KMutableProperty<Color>,
    name: String = "",
    x: Int = 0,
    y: Int = 0
) : ConfigOption(name, x, y) {

    private var value: Color by Delegates.observable(prop.getter.call(Config)) { _, _, new ->
        prop.setter.call(Config, new)
    }
    private val initial = value

    private var redButton: MCButton
    private var greenButton: MCButton
    private var blueButton: MCButton

    private var redHeld: Boolean = false
    private var blueHeld: Boolean = false
    private var greenHeld: Boolean = false

    init {
        val mappedX = (Renderer.screen.getWidth() / 2 - 100 + x).toFloat()
        val mappedY = (Renderer.screen.getWidth() / 2 + 52 + x).toFloat()

        val (r, g, b) = listOf(value.red, value.green, value.blue).map {
            MathLib.map(it.toFloat(), 0f, 255f, mappedX, mappedY).toInt()
        }

        //#if MC==10809
        redButton = MCButton(0, r, y + 15, 5, 10, "")
        greenButton = MCButton(0, g, y + 30, 5, 10, "")
        blueButton = MCButton(0, b, y + 45, 5, 10, "")
        //#else
        //$$ redButton = MCButton(r, y + 15, 5, 10, MCStringTextComponent(""), Button.IPressable {
        //$$     if (!hidden) {
        //$$         redHeld = true
        //$$         it.playDownSound(Client.getMinecraft().soundHandler)
        //$$     }
        //$$ })
        //$$ greenButton = MCButton(g, y + 30, 5, 10, MCStringTextComponent(""), Button.IPressable {
        //$$     if (!hidden) {
        //$$         greenHeld = true
        //$$         it.playDownSound(Client.getMinecraft().soundHandler)
        //$$     }
        //$$ })
        //$$ blueButton = MCButton(b, y + 45, 5, 10, MCStringTextComponent(""), Button.IPressable {
        //$$     if (!hidden) {
        //$$         blueHeld = true
        //$$         it.playDownSound(Client.getMinecraft().soundHandler)
        //$$     }
        //$$ })
        //#endif
    }

    //#if MC==10809
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
    //#else
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //#endif
        if (hidden) return

        val middle = Renderer.screen.getWidth() / 2

        Rectangle(-0x80000000, (middle - 105 + x).toFloat(), (y - 5).toFloat(), 210f, 65f)
            .setShadow(-0x30000000, 3f, 3f)
            .draw()
        Text(name, (middle - 100 + x).toFloat(), y.toFloat()).draw()

        // red slider
        Rectangle(-0x560000, (middle - 100 + x).toFloat(), (y + 19).toFloat(), 155f, 3f)
            .setOutline(-0x1000000, 1f)
            .draw()

        //#if MC==10809
        redButton.xPosition = MathLib.map(
            value.red.toFloat(),
            0f,
            255f,
            (middle - 100 + x).toFloat(),
            (middle + 52 + x).toFloat()
        ).toInt()
        redButton.drawButton(Client.getMinecraft(), mouseX, mouseY)
        //#else
        //$$ redButton.x = MathLib.map(value.red.toFloat(), 0f, 255f, (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat()).toInt()
        //$$ redButton.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif

        // green slider
        Rectangle(-0xff7800, (middle - 100 + x).toFloat(), (y + 34).toFloat(), 155f, 3f)
            .setOutline(-0x1000000, 1f)
            .draw()

        //#if MC==10809
        greenButton.xPosition = MathLib.map(
            value.green.toFloat(),
            0f,
            255f,
            (middle - 100 + x).toFloat(),
            (middle + 52 + x).toFloat()
        ).toInt()
        greenButton.drawButton(Client.getMinecraft(), mouseX, mouseY)
        //#else
        //$$ greenButton.x = MathLib.map(value.green.toFloat(), 0f, 255f, (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat()).toInt()
        //$$ greenButton.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif

        // blue slider
        Rectangle(-0xffff34, (middle - 100 + x).toFloat(), (y + 49).toFloat(), 155f, 3f)
            .setOutline(-0x1000000, 1f)
            .draw()
        //#if MC==10809
        blueButton.xPosition = MathLib.map(
            value.blue.toFloat(),
            0f,
            255f,
            (middle - 100 + x).toFloat(),
            (middle + 52 + x).toFloat()
        ).toInt()
        blueButton.drawButton(Client.getMinecraft(), mouseX, mouseY)
        //#else
        //$$ blueButton.x = MathLib.map(value.blue.toFloat(), 0f, 255f, (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat()).toInt()
        //$$ blueButton.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif

        // color preview
        Rectangle(value.rgb.toLong(), (middle + x + 60).toFloat(), (y + 15).toFloat(), 40f, 40f)
            .setOutline(-0x1000000, 1f)
            .draw()

        handleHeldButtons(mouseX, middle)

        //#if MC==10809
        super.draw(mouseX, mouseY, partialTicks)
        //#else
        //$$ super.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif
    }

    private fun handleHeldButtons(mouseX: Int, middle: Int) {
        if (redHeld) {
            //#if MC<=10809
            redButton.xPosition = mouseX - 1
            //#else
            //$$ redButton.x = mouseX - 1
            //#endif

            limitHeldButton(redButton)
            value = Color(
                MathLib.map(
                    //#if MC<=10809
                    redButton.xPosition.toFloat(),
                    //#else
                    //$$ redButton.x.toFloat(),
                    //#endif
                    (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat(), 0f, 255f
                ).toInt(),
                value.green,
                value.blue
            )
        }
        if (greenHeld) {
            //#if MC<=10809
            greenButton.xPosition = mouseX - 1
            //#else
            //$$ greenButton.x = mouseX - 1
            //#endif

            limitHeldButton(greenButton)
            value = Color(
                value.red,
                MathLib.map(
                    //#if MC<=10809
                    greenButton.xPosition.toFloat(),
                    //#else
                    //$$ greenButton.x.toFloat(),
                    //#endif
                    (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat(), 0f, 255f
                ).toInt(),
                value.blue
            )
        }
        if (blueHeld) {
            //#if MC<=10809
            blueButton.xPosition = mouseX - 1
            //#else
            //$$ blueButton.x = mouseX - 1
            //#endif

            limitHeldButton(blueButton)
            value = Color(
                value.red,
                value.green,
                MathLib.map(
                    //#if MC<=10809
                    blueButton.xPosition.toFloat(),
                    //#else
                    //$$ blueButton.x.toFloat(),
                    //#endif
                    (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat(), 0f, 255f
                ).toInt()
            )
        }
    }

    private fun limitHeldButton(button: MCButton) {
        //#if MC<=10809
        if (button.xPosition < Renderer.screen.getWidth() / 2 - 100 + x)
            button.xPosition = Renderer.screen.getWidth() / 2 - 100 + x
        if (button.xPosition > Renderer.screen.getWidth() / 2 + 52 + x)
            button.xPosition = Renderer.screen.getWidth() / 2 + 52 + x
        //#else
        //$$ if (button.x < Renderer.screen.getWidth() / 2 - 100 + x)
        //$$     button.x = Renderer.screen.getWidth() / 2 - 100 + x
        //$$ if (button.x > Renderer.screen.getWidth() / 2 + 52 + x)
        //$$     button.x = Renderer.screen.getWidth() / 2 + 52 + x
        //#endif
    }

    //#if MC==10809
    override fun mouseClicked(mouseX: Int, mouseY: Int) {
        if (hidden) return

        if (redButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            redHeld = true
            redButton.playPressSound(Client.getMinecraft().soundHandler)
        }
        if (greenButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            greenHeld = true
            greenButton.playPressSound(Client.getMinecraft().soundHandler)
        }
        if (blueButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            blueHeld = true
            blueButton.playPressSound(Client.getMinecraft().soundHandler)
        }

        if (resetButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            value = initial
            val middle = Renderer.screen.getWidth() / 2
            redButton.xPosition = MathLib.map(
                value.red.toFloat(),
                0f,
                255f,
                (middle - 100 + x).toFloat(),
                (middle + 52 + x).toFloat()
            ).toInt()
            greenButton.xPosition = MathLib.map(
                value.green.toFloat(),
                0f,
                255f,
                (middle - 100 + x).toFloat(),
                (middle + 52 + x).toFloat()
            ).toInt()
            blueButton.xPosition = MathLib.map(
                value.blue.toFloat(),
                0f,
                255f,
                (middle - 100 + x).toFloat(),
                (middle + 52 + x).toFloat()
            ).toInt()
        }
    }
    //#else
    //$$ override fun onReset() {
    //$$     if (hidden)
    //$$         return
    //$$
    //$$     value = initial
    //$$     val middle = Renderer.screen.getWidth() / 2
    //$$     redButton.x = MathLib.map(value.red.toFloat(), 0f, 255f, (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat()).toInt()
    //$$     greenButton.x = MathLib.map(value.green.toFloat(), 0f, 255f, (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat()).toInt()
    //$$     blueButton.x = MathLib.map(value.blue.toFloat(), 0f, 255f, (middle - 100 + x).toFloat(), (middle + 52 + x).toFloat()).toInt()
    //$$ }
    //$$
    //$$ override fun getWidgets(): List<Widget> = super.getWidgets() + listOf(redButton, greenButton, blueButton)
    //#endif

    override fun mouseReleased() {
        redHeld = false
        blueHeld = false
        greenHeld = false
    }
}

class SpecialConfigColor(
    prop: KMutableProperty<Color>,
    name: String = "",
    x: Int = 0,
    y: Int = 0
) : ConfigColor(prop, name, x, y) {
    //#if MC==10809
     override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
         super.draw(mouseX, mouseY, partialTicks)
    //#else
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //$$     super.render(matrixStack, mouseX, mouseY, partialTicks)
    //#endif
        hidden = !Config.customTheme
    }
}
