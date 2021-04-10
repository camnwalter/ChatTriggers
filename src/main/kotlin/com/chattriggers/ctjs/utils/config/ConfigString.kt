package com.chattriggers.ctjs.utils.config

import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.minecraft.libs.renderer.Rectangle
import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.utils.kotlin.MCStringTextComponent
import java.io.File
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty

//#if MC==10809
import net.minecraft.client.gui.GuiTextField
//#else
//$$ import com.mojang.blaze3d.matrix.MatrixStack
//$$ import net.minecraft.client.gui.widget.TextFieldWidget
//$$ import net.minecraft.client.gui.widget.Widget
//#endif

class ConfigString(
    private val prop: KMutableProperty<String>,
    name: String = "",
    x: Int = 0,
    y: Int = 0
) : ConfigOption(name, x, y) {
    private var value: String by Delegates.observable(prop.getter.call(Config)) { _, _, new ->
        prop.setter.call(Config, new)
    }
    private val initial = value

    //#if MC==10809
    private val textField = GuiTextField(
        0,
        Renderer.getFontRenderer(),
        Renderer.screen.getWidth() / 2 - 100 + x,
        y + 15,
        200,
        20
    )
    //#else
    //$$ private val textField = TextFieldWidget(
    //$$     Renderer.getFontRenderer(),
    //$$     Renderer.screen.getWidth() / 2 - 100 + x,
    //$$     y + 15,
    //$$     200,
    //$$     20,
    //$$     MCStringTextComponent("")
    //$$ )
    //#endif

    private var systemTime: Long = 0
    private var isValid: Boolean = false
    var isDirectory: Boolean

    private val isValidColor: String
        get() = if (isValid) ChatLib.addColor("&a") else ChatLib.addColor("&c")

    init {
        systemTime = Client.getSystemTime()
        isValid = true
        isDirectory = false

        updateValidDirectory(value)

        //#if MC==10809
        //$$ textField.maxStringLength = 100
        //#else
        textField.setMaxStringLength(100)
        //#endif
        textField.text = isValidColor + value
    }

    private fun updateValidDirectory(directory: String) {
        isValid = !isDirectory || File(directory).isDirectory
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

        update()

        val middle = Renderer.screen.getWidth() / 2

        Rectangle(-0x80000000, (middle - 105 + x).toFloat(), (y - 5).toFloat(), 210f, 45f)
            .setShadow(-0x30000000, 3f, 3f)
            .draw()
        Text(name, (middle - 100 + x).toFloat(), y.toFloat()).draw()

        //#if MC==10809
        textField.xPosition = middle - 100 + x
        textField.drawTextBox()
        //#else
        //$$ textField.x = middle - 100 + x
        //$$ textField.render(matrixStack, mouseX, mouseY, partialTicks)
        //#endif
    }

    private fun update() {
        while (systemTime < Client.getSystemTime() + 50) {
            systemTime += 50
            //#if MC==10809
            textField.updateCursorCounter()
            //#else
            //$$ textField.tick()
            //#endif
        }
    }

    //#if MC==10809
    override fun mouseClicked(mouseX: Int, mouseY: Int) {
        if (hidden)
            return

        textField.mouseClicked(mouseX, mouseY, 0)

        if (resetButton.mousePressed(Client.getMinecraft(), mouseX, mouseY)) {
            value = initial
            textField.text = isValidColor + value
            resetButton.playPressSound(Client.getMinecraft().soundHandler)
        }
    }
    //#else
    //$$ override fun onReset() {
    //$$     if (hidden)
    //$$         return
    //$$
    //$$     value = initial
    //$$     textField.text = isValidColor + value
    //$$ }
    //$$
    //$$ override fun getWidgets(): List<Widget> = super.getWidgets() + listOf(textField)
    //#endif

    //#if MC==10809
    override fun keyTyped(typedChar: Char, keyCode: Int) {
    //#else
    //$$ override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
    //$$     if (hidden || !textField.isFocused)
    //$$         return
    //$$
    //$$     textField.keyReleased(keyCode, scanCode, modifiers)
    //$$ }
    //$$
    //$$ override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) {
    //#endif
        if (hidden || !textField.isFocused)
            return

        //#if MC==10809
        textField.textboxKeyTyped(typedChar, keyCode)
        //#else
        //$$ textField.keyPressed(keyCode, scanCode, modifiers)
        //#endif

        val text = ChatLib.removeFormatting(textField.text)
        updateValidDirectory(text)
        textField.text = isValidColor + text

        if (isValid)
            value = text
    }
}
