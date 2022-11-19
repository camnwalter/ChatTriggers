package com.chattriggers.ctjs.mixins.gui;

//#if MC>=11701
//$$ import com.chattriggers.ctjs.mixins.gui.ChatComponentAccessor;
//$$ import com.chattriggers.ctjs.minecraft.libs.ChatLib;
//$$ import com.chattriggers.ctjs.minecraft.listeners.events.CancellableEvent;
//$$ import com.chattriggers.ctjs.minecraft.wrappers.Client;
//$$ import com.chattriggers.ctjs.triggers.TriggerType;
//$$ import gg.essential.universal.wrappers.message.UTextComponent;
//$$ import net.minecraft.client.GuiMessage;
//$$ import net.minecraft.client.gui.components.ChatComponent;
//$$ import net.minecraft.client.gui.screens.ChatScreen;
//$$ import net.minecraft.util.FormattedCharSequence;
//$$ import net.minecraft.util.Mth;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$
//$$ @Mixin(ChatScreen.class)
//$$ public class ChatScreenMixin {
//$$     @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;getClickedComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;"), cancellable = true)
//$$     private void chattriggers_chatComponentClickedTrigger(double d, double e, int i, CallbackInfoReturnable<Boolean> cir) {
//$$         double g = Client.getMinecraft().getWindow().getGuiScaledHeight() - e - 40.0;
//$$
//$$         ChatComponent chat = Client.getMinecraft().gui.getChat();
//$$
//$$         g = Mth.floor(g / (chat.getScale() * (Client.getMinecraft().options.chatLineSpacing + 1.0)));
//$$
//$$         int j = (int)(g / 9d + (double)((ChatComponentAccessor) chat).getChatScrollbarPos());
//$$         if (j < 0 || j >= ((ChatComponentAccessor) chat).getTrimmedMessages().size()) {
//$$             return;
//$$         }
//$$
//$$         GuiMessage<FormattedCharSequence> message = ((ChatComponentAccessor) chat).getTrimmedMessages().get(j);
//$$
//$$         CancellableEvent event = new CancellableEvent();
//$$         TriggerType.ChatComponentClicked.triggerAll(ChatLib.formattedCharSequenceToComponent(message.getMessage()), event);
//$$         if (event.isCanceled()) {
//$$             cir.setReturnValue(false);
//$$         }
//$$     }
//$$ }
//#endif
