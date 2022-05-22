package com.chattriggers.ctjs.minecraft.objects.display

import com.chattriggers.ctjs.engine.ILoader
import com.chattriggers.ctjs.minecraft.libs.renderer.Renderer
import com.chattriggers.ctjs.minecraft.libs.renderer.Text
import com.chattriggers.ctjs.minecraft.listeners.MouseListener
import com.chattriggers.ctjs.minecraft.wrappers.Client
import com.chattriggers.ctjs.triggers.RegularTrigger
import com.chattriggers.ctjs.triggers.Trigger
import com.chattriggers.ctjs.triggers.TriggerType
import org.mozilla.javascript.NativeObject

/**
 * DisplayLine can be initialized with certain properties. These properties
 * should be passed through a normal JavaScript object. They can be accessed
 * and changed later.
 *
 * PROPERTIES (all optional):
 * - textColor (Long) - the color of the text, see [setTextColor]
 * - backgroundColor (Long) - the color of the background of the line, see [setBackgroundColor]
 * - align (DisplayHandler.Align) - the alignment of the text, see [setAlign]
 * - backgroundType (DisplayHandler.BackgroundType) - the type of background, see [setBackgroundType]
 *
 * @param config the JavaScript config object
 */
abstract class DisplayLine {
    private lateinit var text: Text

    private var textWidth = 0f

    private var textColor: Long? = null
    private var backgroundColor: Long? = null
    private var backgroundType: DisplayHandler.BackgroundType? = null
    private var align: DisplayHandler.Align? = null

    private var onClicked: Trigger? = null
    private var onHovered: Trigger? = null
    private var onMouseLeave: Trigger? = null
    private var onPreDraw: Trigger? = null
    private var onPostDraw: Trigger? = null

    private var hovered: Boolean = false
    private var cachedX = 0.0
    private var cachedY = 0.0
    private var cachedWidth = 0.0
    private var cachedHeight = 0.0

    internal var visible: Boolean = true

    constructor(text: String) {
        setText(text)
    }

    constructor(text: String, config: NativeObject) {
        setText(text)

        textColor = config.getOption("textColor", null)?.toLong()
        backgroundColor = config.getOption("backgroundColor", null)?.toLong()

        setAlign(config.getOption("align", null))
        setBackgroundType(config.getOption("backgroundType", null))
    }

    private fun NativeObject?.getOption(key: String, default: Any?): String? {
        return (this?.get(key) ?: default)?.toString()
    }

    init {
        MouseListener.registerClickListener { x, y, button, pressed ->
            if (
                visible &&
                x in cachedX..cachedX + cachedWidth &&
                y in cachedY..cachedY + cachedHeight
            ) {
                onClicked?.trigger(arrayOf(x, y, button, pressed))
            }
        }
    }

    fun getText(): Text = text

    fun setText(text: String) = apply {
        this.text = Text(text)
        textWidth = Renderer.getStringWidth(text) * this.text.getScale()
    }

    fun getTextColor(): Long? = textColor

    fun setTextColor(color: Long?) = apply {
        textColor = color
    }

    fun getTextWidth(): Float = textWidth

    fun setShadow(shadow: Boolean) = apply { text.setShadow(shadow) }

    fun setScale(scale: Float) = apply {
        text.setScale(scale)
        textWidth = Renderer.getStringWidth(text.getString()) * scale
    }

    fun getAlign(): DisplayHandler.Align? = align

    fun setAlign(align: Any?) = apply {
        this.align = when (align) {
            is String -> DisplayHandler.Align.valueOf(align.uppercase())
            is DisplayHandler.Align -> align
            else -> null
        }
    }

    fun getBackgroundType(): DisplayHandler.BackgroundType? = backgroundType

    fun setBackgroundType(backgroundType: Any?) = apply {
        this.backgroundType = when (backgroundType) {
            is String -> DisplayHandler.BackgroundType.valueOf(backgroundType.uppercase().replace(" ", "_"))
            is DisplayHandler.BackgroundType -> backgroundType
            else -> null
        }
    }

    fun getBackgroundColor(): Long? = backgroundColor

    fun setBackgroundColor(color: Long?) = apply {
        backgroundColor = color
    }

    fun registerClicked(method: Any) = run {
        onClicked = RegularTrigger(method, TriggerType.Other, getLoader())
        onClicked
    }

    fun registerHovered(method: Any) = run {
        onHovered = RegularTrigger(method, TriggerType.Other, getLoader())
        onHovered
    }

    fun registerMouseLeave(method: Any) = run {
        onMouseLeave = RegularTrigger(method, TriggerType.Other, getLoader())
        onMouseLeave
    }

    /**
     * This register allows you to call a method right before the DisplayLine is drawn.
     *
     * @param method The method to call.
     */
    fun registerPreDraw(method: Any) = run {
        onPreDraw = RegularTrigger(method, TriggerType.Other, getLoader())
        onPreDraw
    }

    /**
     * This register allows you to call a method right after the DisplayLine is drawn.
     *
     * @param method The method to call.
     */
    fun registerPostDraw(method: Any) = run {
        onPostDraw = RegularTrigger(method, TriggerType.Other, getLoader())
        onPostDraw
    }

    internal abstract fun getLoader(): ILoader

    fun draw (
        x: Float,
        y: Float,
        totalWidth: Float,
        backgroundType_: DisplayHandler.BackgroundType,
        backgroundColor_: Long,
        textColor_: Long,
        align: DisplayHandler.Align
    ) {
        onPreDraw?.trigger(arrayOf(this))

        val backgroundType = this.backgroundType ?: backgroundType_
        val backgroundColor = this.backgroundColor ?: backgroundColor_
        val textColor = this.textColor ?: textColor_

        //X relative to the top left of the display
        val baseX = when (align) {
            DisplayHandler.Align.LEFT -> x
            DisplayHandler.Align.CENTER -> x - totalWidth / 2
            DisplayHandler.Align.RIGHT -> x - totalWidth
        }

        if (backgroundType == DisplayHandler.BackgroundType.FULL)
            Renderer.drawRect(backgroundColor, baseX - 1, y - 1, totalWidth + 1, text.getHeight())

        if (text.getString().isEmpty())
            return

        val xOffset = when (this.align ?: align) {
            DisplayHandler.Align.LEFT -> baseX
            DisplayHandler.Align.CENTER -> baseX + (totalWidth - textWidth) / 2
            DisplayHandler.Align.RIGHT -> baseX + (totalWidth - textWidth)
        }

        if (backgroundType == DisplayHandler.BackgroundType.PER_LINE)
            Renderer.drawRect(backgroundColor, xOffset - 1, y - 1, textWidth + 1, text.getHeight())

        text.setX(xOffset).setY(y).setColor(textColor).draw()

        cachedX = baseX - 1.0
        cachedY = y - 2.0
        cachedWidth = totalWidth + 1.0
        cachedHeight = text.getHeight() + 1.0

        if (!visible) return

        if (
            Client.getMouseX() in cachedX..cachedX + cachedWidth &&
            Client.getMouseY() in cachedY..cachedY + cachedHeight
        ) {
            hovered = true
            onHovered?.trigger(
                arrayOf(
                    Client.getMouseX(),
                    Client.getMouseY()
                )
            )
        } else {
            if (hovered) {
                onMouseLeave?.trigger(
                    arrayOf(
                        Client.getMouseX(),
                        Client.getMouseY()
                    )
                )
            }

            hovered = false
        }

        onPostDraw?.trigger(arrayOf(this))
    }

    override fun toString() =
        "DisplayLine{" +
                "text=$text, textColor=$textColor, align=$align" +
                "backgroundType=$backgroundType, backgroundColor=$backgroundColor" +
                "}"
}
