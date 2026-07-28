package net.mt1006.nbtac.mixin.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.mt1006.nbtac.autocomplete.loader.Loader;
import net.mt1006.nbtac.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	@Inject(method = "<init>", at = @At("RETURN"))
	private void atConstructor(GameConfig gameConfig, CallbackInfo ci)
	{
		if (ModConfig.useNewThread.val) { new Thread(Loader::load).start(); }
	}
}
