package net.mt1006.nbtac.mixin.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin
{
	@Inject(method = "getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "HEAD"), remap = false)
	private void atGetCompletionSuggestions(ParseResults<?> parse, int cursor, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		NbtTagManager.clearProvided();
	}
}
