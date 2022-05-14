package com.chattriggers.ctjs.launch.plugin

import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.minecraft.listeners.CancellableEvent
import com.chattriggers.ctjs.minecraft.objects.message.TextComponent
import com.chattriggers.ctjs.triggers.TriggerType
import com.chattriggers.ctjs.utils.kotlin.MCITextComponent
import dev.falsehonesty.asmhelper.dsl.At
import dev.falsehonesty.asmhelper.dsl.InjectionPoint
import dev.falsehonesty.asmhelper.dsl.code.CodeBlock.Companion.methodReturn
import dev.falsehonesty.asmhelper.dsl.inject
import dev.falsehonesty.asmhelper.dsl.instructions.Descriptor

fun injectGuiChat() {
    injectTextComponentHover()
}

fun injectTextComponentHover() = inject {
    className = "net/minecraft/client/gui/GuiChat"
    methodName = "drawScreen"
    methodDesc = "(IIF)V"
    at = At(
        InjectionPoint.INVOKE(
            Descriptor(
                "net/minecraft/client/gui/GuiNewChat",
                "getChatComponent",
                "(II)L$ICHAT_COMPONENT;",
            )
        ),
        before = false,
        shift = 1,
    )

    methodMaps = mapOf("func_73863_a" to "drawScreen")

    codeBlock {
        val local1 = shadowLocal<Int>()
        val local2 = shadowLocal<Int>()
        val local4 = shadowLocal<MCITextComponent?>()

        code {
            if (local4 != null) {
                TextComponent.hoverListeners.forEach { (textComponent, cb) ->
                    if (textComponent.chatComponentText == local4) {
                        val hoverValue = cb() ?: return@forEach

                        textComponent.setHoverValue(hoverValue)
                        val parent = textComponent.parent

                        ChatLib.editChat(parent.getChatLineId(), parent)
                    }
                }

                val event = CancellableEvent()
                TriggerType.ChatComponentHovered.triggerAll(TextComponent(local4), local1, local2, event)
                if (event.isCancelled())
                    methodReturn()
            }
        }
    }
}
