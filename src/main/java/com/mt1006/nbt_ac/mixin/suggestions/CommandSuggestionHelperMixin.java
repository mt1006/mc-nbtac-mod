package com.mt1006.nbt_ac.mixin.suggestions;

import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import net.minecraft.client.gui.CommandSuggestionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestionHelper.class)
public class CommandSuggestionHelperMixin
{
	@Inject(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;", remap = false))
	public void atUpdateCommandInfo(CallbackInfo ci)
	{
		NbtSuggestionManager.subtextMap.clear();
	}
}
