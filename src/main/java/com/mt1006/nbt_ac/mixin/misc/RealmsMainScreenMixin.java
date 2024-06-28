package com.mt1006.nbt_ac.mixin.misc;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.config.gui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsMainScreen.class)
public class RealmsMainScreenMixin
{
	@Shadow @Mutable @Final private Screen lastScreen;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void atInit(CallbackInfo ci)
	{
		if (!ModConfig.debugConfigScreen.val) { return; }
		lastScreen = new ConfigScreen(lastScreen);
	}
}
