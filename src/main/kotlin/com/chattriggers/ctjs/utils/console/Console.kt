package com.chattriggers.ctjs.utils.console

import com.chattriggers.ctjs.Reference
import com.chattriggers.ctjs.engine.ILoader
import com.chattriggers.ctjs.utils.Config
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.DefaultCaret

class Console(val loader: ILoader?) {
    private val frame = JFrame(
        "ChatTriggers ${Reference.MODVERSION} ${loader?.getLanguage()?.langName ?: "Default"} Console"
    )

    private val textArea = JTextPane()
    private val inputField = RSyntaxTextArea(5, 1).apply {
        syntaxEditingStyle = loader?.getLanguage()?.syntaxStyle ?: SyntaxConstants.SYNTAX_STYLE_NONE
        Theme.load(javaClass.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml")).apply(this)
        margin = Insets(5, 5, 5, 5)
        isCodeFoldingEnabled = true
    }

    private val components = mutableSetOf<Component>(textArea)
    private val history = mutableListOf<String>()
    private var historyOffset = 0

    val writer = TextAreaWriter(textArea)

    init {
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE

        textArea.isEditable = false

        textArea.margin = Insets(5, 5, 5, 5)
        textArea.autoscrolls = true
        val caret = textArea.caret as DefaultCaret
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE

        inputField.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {}

            override fun keyPressed(e: KeyEvent) {}

            override fun keyReleased(e: KeyEvent) {
                if (!e.isControlDown) return

                when (e.keyCode) {
                    KeyEvent.VK_ENTER -> {
                        val command = inputField.text
                        inputField.text = ""
                        history.add(command)
                        historyOffset = 0

                        if (command == "help") {
                            writer.println("""
                                -------------- ChatTriggers Console Help --------------
                                 Shortcuts:
                                  Control + Enter: Run code in the textbox
                                  Control + UP / DOWN: Cycle between ran code history
                                  Control + L: Clear console
                                  Control + R: Reload ChatTriggers
                                -------------------------------------------------------
                            """.trimIndent())
                        } else {
                            writer.println("eval> ${command.prependIndent("    > ").substring(6)}")

                            try {
                                writer.println(loader?.eval(command) ?: return)
                            } catch (e: Throwable) {
                                printStackTrace(e)
                            }
                        }
                    }

                    KeyEvent.VK_UP -> {
                        historyOffset++

                        try {
                            val message = history[history.size - historyOffset]
                            inputField.text = message
                        } catch (exception: Exception) {
                            historyOffset--
                        }
                    }

                    KeyEvent.VK_DOWN -> {
                        historyOffset--

                        if (historyOffset < 0) historyOffset = 0

                        try {
                            val message = history[history.size - historyOffset]
                            inputField.text = message
                        } catch (exception: Exception) {
                            historyOffset = 0
                            inputField.text = ""
                        }
                    }

                    KeyEvent.VK_L -> {
                        clearConsole()
                    }

                    KeyEvent.VK_R -> {
                        Reference.loadCT()
                    }
                }
            }
        })

        frame.add(JScrollPane(textArea))
        frame.add(inputField, BorderLayout.SOUTH)
        frame.pack()
        frame.isVisible = false
        frame.setSize(800, 600)
    }

    fun clearConsole() {
        SwingUtilities.invokeLater {
            writer.clear()
        }
    }

    // TODO: Make println and roll this back before 3.0.0
    @JvmOverloads
    fun println(obj: Any, logType: LogType = LogType.INFO, end: String = "\n", customColor: Color? = null) {
        SwingUtilities.invokeLater {
            try {
                writer.println(obj.toString(), logType, end, customColor)
            } catch (exception: Exception) {
                println(obj.toString(), logType, end, customColor)
            }
        }
    }

    fun printStackTrace(error: Throwable) {
        SwingUtilities.invokeLater {
            try {
                if (Config.openConsoleOnError)
                    showConsole()

                val index = error.stackTrace.indexOfFirst {
                    it?.fileName?.lowercase()?.contains("jsloader") ?: false
                }

                error.stackTrace = error.stackTrace.dropLast(error.stackTrace.size - index - 1).map {
                    val fileNameIndex = it.fileName?.indexOf("ChatTriggers/modules/") ?: return@map it
                    val classNameIndex = it.className.indexOf("ChatTriggers_modules_")

                    if (fileNameIndex != -1) {
                        StackTraceElement(
                            it.className.substring(classNameIndex + 21),
                            it.methodName,
                            it.fileName!!.substring(fileNameIndex + 21),
                            it.lineNumber
                        )
                    } else it
                }.toTypedArray()

                printErrorWithColor(error)
            } catch (ignored: Throwable) {
                error.printStackTrace()
            }
        }
    }

    private fun printErrorWithColor(error: Throwable) {
        val sw = StringWriter()
        error.printStackTrace(PrintWriter(sw))
        val stacktrace = sw.toString()

        writer.println(stacktrace, LogType.ERROR)
    }

    fun showConsole() {
        frame.isVisible = true

        val bg = Config.consoleBackgroundColor
        val fg = Config.consoleForegroundColor

        for (comp in components) {
            comp.background = bg
            comp.foreground = fg
        }

        frame.toFront()
        frame.repaint()
        frame.revalidate()

        val chosenFont =
            if (Config.consoleFiraCodeFont) FIRA_FONT.deriveFont(Config.consoleFontSize.toFloat()) else Font(
                "DejaVu Sans Mono",
                Font.PLAIN,
                15
            ).deriveFont(Config.consoleFontSize.toFloat())

        textArea.font = chosenFont
        // TODO: Ligatures make everything extremely slow for some reason. Is this fixable?
//        val attrs = FIRA_FONT.attributes.apply { (this as MutableMap<TextAttribute, Any>)[TextAttribute.LIGATURES] = TextAttribute.LIGATURES_ON }
//        inputField.font = FIRA_FONT.deriveFont(attrs)
        inputField.font = chosenFont

        frame.toFront()
        frame.repaint()
        frame.revalidate()
    }

    companion object {
        val FIRA_FONT: Font = Font.createFont(
            Font.TRUETYPE_FONT,
            System.getProperty("ct.firaFile", null)?.let { File(it).inputStream() }
                ?: this::class.java.getResourceAsStream("/FiraCode-Regular.otf")
        ).deriveFont(9f)

        init {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FIRA_FONT)
        }
    }
}
