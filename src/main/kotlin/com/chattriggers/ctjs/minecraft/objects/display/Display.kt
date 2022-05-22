package com.chattriggers.ctjs.minecraft.objects.display

import com.chattriggers.ctjs.engine.ILoader
import com.chattriggers.ctjs.triggers.RegularTrigger
import com.chattriggers.ctjs.triggers.Trigger
import com.chattriggers.ctjs.triggers.TriggerType
import com.chattriggers.ctjs.utils.kotlin.getOption
import org.mozilla.javascript.NativeObject
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Instances a new Display with certain properties. These properties
 * should be passed through as a normal JavaScript object.
 *
 * PROPERTIES (all optional):
 * - x (Number) - The x position of the display, see [setX]
 * - y (Number) - The y position of the display, see [setY]
 * - minWidth (Number) - The minimum width of the display, see [setMinWidth]
 * - backgroundColor (Long) - the background color of the display, see [setBackgroundColor]
 * - textColor (Long) - the text color of the display, see [setTextColor]
 * - backgroundType (Display.BackgroundType) - the background type of the display, see [setBackgroundType]
 * - align (Display.Align) - the alignment of the display, see [setAlign]
 * - order (Display.Order) - the order of the display, see [setOrder]
 * - registerType (Display.RegisterType) - the register type of the display, see [setRegisterType]
 * - visible (Boolean) - whether the display is visible, see [show] and [hide]
 *
 * @param config the JavaScript config object
 */
abstract class Display() {
    private var lines = CopyOnWriteArrayList<DisplayLine>()

    private var x = 0f
    private var y = 0f
    private var order = DisplayHandler.Order.DOWN

    private var backgroundColor: Long = 0x50000000
    private var textColor: Long = 0xffffffff
    private var backgroundType = DisplayHandler.BackgroundType.NONE
    private var align = DisplayHandler.Align.LEFT

    internal var registerType = DisplayHandler.RegisterType.RENDER_OVERLAY
    private var visible = true

    private var minWidth = 0f
    private var width = 0f
    private var height = 0f

    private var onPreDraw: Trigger? = null
    private var onPostDraw: Trigger? = null

    init {
        @Suppress("LeakingThis")
        DisplayHandler.registerDisplay(this)
    }

    constructor(config: NativeObject?) : this() {
        setX(config.getOption("x", 0f).toFloat())
        setY(config.getOption("y", 0f).toFloat())
        setMinWidth(config.getOption("minWidth", 0f).toFloat())
        setBackgroundColor(config.getOption("backgroundColor", 0x50000000).toLong())
        setTextColor(config.getOption("textColor", 0xffffffff).toLong())
        setBackgroundType(config.getOption("backgroundType", DisplayHandler.BackgroundType.NONE))
        setAlign(config.getOption("align", DisplayHandler.Align.LEFT))
        setOrder(config.getOption("order", DisplayHandler.Order.DOWN))
        setRegisterType(config.getOption("registerType", DisplayHandler.RegisterType.RENDER_OVERLAY))
        if (config.getOption("visible", true).toBoolean()) show() else hide()
    }

    fun getX(): Float = x

    fun setX(x: Float) = apply {
        this.x = x
    }

    fun getY(): Float = y

    fun setY(y: Float) = apply {
        this.y = y
    }

    fun getWidth(): Float = width

    fun getHeight(): Float = height

    fun getMinWidth(): Float = minWidth

    fun setMinWidth(minWidth: Float) = apply {
        this.minWidth = minWidth
    }

    fun getBackgroundColor(): Long = backgroundColor

    fun setBackgroundColor(backgroundColor: Long) = apply {
        this.backgroundColor = backgroundColor
    }

    fun getTextColor(): Long = textColor

    fun setTextColor(textColor: Long) = apply {
        this.textColor = textColor
    }

    fun getBackgroundType(): DisplayHandler.BackgroundType = backgroundType

    fun setBackgroundType(backgroundType: Any) = apply {
        this.backgroundType = when (backgroundType) {
            is String -> DisplayHandler.BackgroundType.valueOf(backgroundType.uppercase().replace(" ", "_"))
            is DisplayHandler.BackgroundType -> backgroundType
            else -> DisplayHandler.BackgroundType.NONE
        }
    }

    fun getAlign(): DisplayHandler.Align = align

    fun setAlign(align: Any) = apply {
        this.align = when (align) {
            is String -> DisplayHandler.Align.valueOf(align.uppercase())
            is DisplayHandler.Align -> align
            else -> DisplayHandler.Align.LEFT
        }
    }

    fun getOrder(): DisplayHandler.Order = order

    fun setOrder(order: Any) = apply {
        this.order = when (order) {
            is String -> DisplayHandler.Order.valueOf(order.uppercase())
            is DisplayHandler.Order -> order
            else -> DisplayHandler.Order.DOWN
        }
    }

    fun getRegisterType(): DisplayHandler.RegisterType = registerType

    fun setRegisterType(registerType: Any) = apply {
        this.registerType = when (registerType) {
            is String -> DisplayHandler.RegisterType.valueOf(registerType.uppercase().replace(" ", "_"))
            is DisplayHandler.RegisterType -> registerType
            else -> DisplayHandler.RegisterType.RENDER_OVERLAY
        }
    }

    fun getIndexOfLine(line: DisplayLine): Int {
        return lines.indexOf(line)
    }

    fun getLine(line: Any): Any? {
        return when (line) {
            is Int -> lines[line]
            is String -> lines.find { it.getText().getString() == line }
            else -> null
        }
    }

    fun setLine(index: Int, line: Any) = apply {
        while (lines.size - 1 < index) {
            lines.add(createDisplayLine(""))
        }

        lines[index] = when (line) {
            is String -> createDisplayLine(line)
            is DisplayLine -> line
            else -> createDisplayLine("")
        }
    }

    fun getLines(): List<DisplayLine> = lines

    fun setLines(lines: MutableList<DisplayLine>) = apply {
        this.lines = CopyOnWriteArrayList(lines)
    }

    @JvmOverloads
    fun addLine(index: Int = -1, line: Any) = apply {
        val toAdd = when (line) {
            is String -> createDisplayLine(line)
            is DisplayLine -> line
            else -> createDisplayLine("")
        }

        if (index == -1) {
            lines.add(toAdd)
        } else lines.add(index, toAdd)
    }

    /**
     * Removes a line from the display
     *
     * @param line The index of the line, a string or the DisplayLine to remove
     */
    fun removeLine(line: Any) = apply {
        when (line) {
            is Int -> lines.removeAt(line)
            is DisplayLine -> lines.remove(line)
            is String -> lines.removeIf { it.getText().getString() == line }
            else -> throw IllegalArgumentException("Cannot remove line of type $line")
        }
    }

    fun addLines(vararg lines: Any) = apply {
        this.lines.addAll(lines.map {
            when (it) {
                is String -> createDisplayLine(it)
                is DisplayLine -> it
                else -> createDisplayLine("")
            }
        })
    }

    fun clearLines() = apply {
        lines.clear()
    }

    fun isVisible(): Boolean = visible

    fun show() = apply {
        visible = true
        lines.forEach { it.visible = true }
    }

    fun hide() = apply {
        visible = false
        lines.forEach { it.visible = false }
    }

    /**
     * This register allows you to call a method right before the Display is drawn.
     *
     * @param method The method to call.
     */
    fun registerPreDraw(method: Any) = run {
        onPreDraw = RegularTrigger(method, TriggerType.Other, getLoader())
        onPreDraw
    }

    /**
     * This register allows you to call a method right after the Display is drawn.
     *
     * @param method The method to call.
     */

    fun registerPostDraw(method: Any) = run {
        onPostDraw = RegularTrigger(method, TriggerType.Other, getLoader())
        onPostDraw
    }

    internal fun draw() {
        if (!visible) return

        onPreDraw?.trigger(arrayOf(this))

        width = lines.maxOfOrNull { it.getTextWidth() }?.coerceAtLeast(minWidth) ?: minWidth

        var i = 0f
        lines.forEach {
            it.draw(x, y + i, width, backgroundType, backgroundColor, textColor, align)

            when (order) {
                DisplayHandler.Order.UP -> i -= it.getText().getHeight()
                DisplayHandler.Order.DOWN -> i += it.getText().getHeight()
            }
        }

        height = i

        onPostDraw?.trigger(arrayOf(this))
    }

    internal abstract fun createDisplayLine(text: String): DisplayLine

    internal abstract fun getLoader(): ILoader

    override fun toString() =
        "Display{" +
                "registerType=$registerType, visible=$visible" +
                "x=$x, y=$y, " +
                "backgroundType=$backgroundType, backgroundColor=$backgroundColor, " +
                "textColor=$textColor, align=$align, order=$order, " +
                "minWidth=$minWidth, width=$width, height=$height, " +
                "lines=$lines" +
                "}"
}
