package net.mt1006.nbtac.mixin.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.mt1006.nbtac.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(EditBox.class)
public class EditBoxMixin
{
	@Shadow private String value;
	@Shadow private Consumer<String> responder;

	@Inject(method = "moveCursorTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;updateTextPosition()V"))
	private void atMoveCursorTo(int dir, boolean extendSelection, CallbackInfo ci)
	{
		if (Minecraft.getInstance().screen instanceof ChatScreen && ModConfig.updateOnCursorMovement.val && responder != null)
		{
			responder.accept(value);
		}
	}
}
