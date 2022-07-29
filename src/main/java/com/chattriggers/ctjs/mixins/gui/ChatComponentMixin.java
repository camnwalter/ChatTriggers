package com.chattriggers.ctjs.mixins.gui;

//#if MC>=11701
//$$ import com.chattriggers.ctjs.minecraft.wrappers.Client;
//$$ import com.chattriggers.ctjs.triggers.TriggerType;
//$$ import gg.essential.universal.wrappers.message.UTextComponent;
//$$ import net.minecraft.client.GuiMessage;
//$$ import net.minecraft.client.gui.components.ChatComponent;
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.network.chat.Style;
//$$ import net.minecraft.util.Mth;
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$
//$$ import java.util.List;
//$$
//$$ @Mixin(ChatComponent.class)
//$$ public abstract class ChatComponentMixin {
//$$     @Shadow
//$$     @Final
//$$     private List<GuiMessage<Component>> allMessages;
//$$
//$$     @Shadow
//$$     private int chatScrollbarPos;
//$$
//$$     @Shadow
//$$     public abstract double getScale();
//$$
//$$     @Inject(
//$$             method = "getClickedComponentStyleAt",
//$$             at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"),
//$$             cancellable = true
//$$     )
//$$     private void chattriggers_chatComponentClickedTrigger(double d, double e, CallbackInfoReturnable<Style> cir) {
//$$         double g = Client.getMinecraft().getWindow().getGuiScaledHeight() - e - 40.0;
//$$         g = Mth.floor(g / (this.getScale() * (Client.getMinecraft().options.chatLineSpacing + 1.0)));
//$$
//$$         int j = (int)(g / 9d + (double)this.chatScrollbarPos);
//$$         GuiMessage<Component> component = this.allMessages.get(j);
//$$         TriggerType.ChatComponentClicked.triggerAll(new UTextComponent(component.getMessage()), cir);
//$$     }
//$$ }
//#endif
