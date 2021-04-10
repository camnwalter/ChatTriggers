package com.chattriggers.ctjs.utils.config

import com.chattriggers.ctjs.minecraft.libs.renderer.Rectangle
import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.printTraceToConsole
import com.chattriggers.ctjs.utils.kotlin.MCButton
import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty

//#if MC!=10809
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//$$ import net.minecraft.client.gui.widget.Widget
//$$ import net.minecraft.client.gui.widget.button.Button
//#endif

open class ConfigStringSelector(
    private val prop: KMutableProperty<String>,
    private val values: Array<String> = emptyArray(),
    name: String = "",
    x: Int = 0,
    y: Int = 0
) : ConfigOption(name, x, y) {
    private var value: Int by Delegates.observable(
        values.indexOf(prop.getter.call(Config))
    ) { _, _, new ->
        prop.setter.call(Config, values[new])
    }
    private val initial = value

    //#if MC==10809
    private val leftArrowButton = MCButton(
        0,
        Renderer.screen.getWidth() / 2 - 100 + x,
        y + 15,
        30,
        20,
        "<"
    )

    private val rightArrowButton = MCButton(
        0,
        Renderer.screen.getWidth() / 2 + 70 + x,
        y + 15,
        30,
        20,
        ">"
    )
    //#else
    //$$ private val leftArrowButton = MCButton(
    //$$     Renderer.screen.getWidth() / 2 - 100 + x,
    //$$     y + 15,
    //$$     30,
    //$$     20,
    //$$     MCStringTextComponent("<"),
    //$$     Button.IPressable {
    //$$         when {
    //$$             hidden -> return@IPressable
    //$$             value < 1 -> value = values.lastIndex
    //$$             else -> value--
    //$$         }
    //$$     }
    //$$ )
    //$$
    //$$ private val rightArrowButton = MCButton(
    //$$     Renderer.screen.getWidth() / 2 + 70 + x,
    //$$     y + 15,
    //$$     30,
    //$$     20,
    //$$     MCStringTextComponent(">"),
    //$$     Button.IPressable {
    //$$         when {
    //$$             hidden -> return@IPressable
    //$$             value > values.size -> value = 0
    //$$             else -> value++
    //$$         }
    //$$     }
    //$$ )
    //#endif

    fun getValue(): String {
        try {
            return values[value]
        } catch (exception: IndexOutOfBoundsException) {
            if (values.isNotEmpty()) {
                return values[0]
            } else exception.printTraceToConsole()
        }

        return ""
    }

    //#if MC==10809
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.draw(mouseX, mouseY, partialTicks)
    //#else
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //$$     super.render(matrixStack, mouseX, mouseY, partialTicks)
    //#endif
        if (hidden)
            return

        val middle = Renderer.screen.getWidth() / 2

        Rectangle(-0x80000000, (middle - 105 + x).toFloat(), (y - 5).toFloat(), 210f, 45f)
            .setShadow(-0x30000000, 3f, 3f)
            .draw()
        Text(name, (middle - 100 + x).toFloat(), y.toFloat()).draw()

        Text(
            getValue(),
            (middle + x - Renderer.getStringWidth(getValue()) / 2).toFloat(),
            (y + 20).toFloat()
        ).draw()

        //#if MC<=10809
        leftArrowButton.xPosition = middle - 100 + x
        rightArrowButton.xPosition = middle + 70 + x

        leftArrowButton.drawButton(Client.getMinecraft(), mouseX, mouseY)
        rightArrowButton.drawButton(Client.getMinecraft(), mouseX, mouseY)
        //#else
        //$$ leftArrowButton.x = middle - 100 + x
        //$$ rightArrowButton.x = middle + 70 + x
        //$$
        //$$ leftArrowButton.render(matrixStack, mouseX, mouseY, partialTicks)
        //$$ rightArrowButton.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif

    }

    //#if MC==10809
    override fun mouseClicked(mouseX: Int, mouseY: Int) {
        if (hidden) return

        if (leftArrowButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            if (value - 1 < 0) value = values.size - 1
            else value--

            leftArrowButton.playPressSound(Client.getMinecraft().soundHandler)
        } else if (rightArrowButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            if (value + 1 >= values.size) value = 0
            else value++

            rightArrowButton.playPressSound(Client.getMinecraft().soundHandler)
        }

        if (resetButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            value = initial
            resetButton.playPressSound(Client.getMinecraft().soundHandler)
        }
    }
    //#else
    //$$ override fun onReset() {
    //$$     value = initial
    //$$ }
    //$$
    //$$ override fun getWidgets(): List<Widget> = super.getWidgets() + listOf(leftArrowButton, rightArrowButton)
    //#endif
}

class ConsoleThemeSelector(
    prop: KMutableProperty<String>,
    name: String = "",
    x: Int = 0,
    y: Int = 0
) : ConfigStringSelector(
    prop,
    arrayOf(
        "default.dark",
        "ashes.dark",
        "atelierforest.dark",
        "isotope.dark",
        "codeschool.dark",
        "gotham",
        "hybrid",
        "3024.light",
        "chalk.light",
        "blue",
        "slate",
        "red",
        "green",
        "aids"
    ),
    name,
    x,
    y
) {
    //#if MC==10809
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.draw(mouseX, mouseY, partialTicks)
    //#else
    //$$ override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
    //$$     super.render(matrixStack, mouseX, mouseY, partialTicks)
    //#endif
        hidden = Config.customTheme
    }
}
