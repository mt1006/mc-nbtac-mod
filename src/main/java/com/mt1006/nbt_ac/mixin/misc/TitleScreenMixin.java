package com.mt1006.nbt_ac.mixin.misc;

import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.config.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin
{
	@Inject(method = "realmsButtonClicked", at = @At(value = "HEAD"), cancellable = true)
	private void atGetCompletionSuggestions(CallbackInfo ci)
	{
		if (!ModConfig.debugConfigScreen.val) { return; }
		Minecraft.getInstance().setScreen(new ConfigScreen((TitleScreen)(Object)this));
		ci.cancel();
	}
}
