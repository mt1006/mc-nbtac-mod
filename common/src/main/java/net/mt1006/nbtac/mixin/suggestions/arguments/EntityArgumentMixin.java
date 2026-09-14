package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.EntityArgument;
import net.mt1006.nbtac.utils.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityArgument.class)
public class EntityArgumentMixin
{
	@Inject(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;fillSuggestions(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"))
	private void atListSuggestions(CommandContext<?> ctx, SuggestionsBuilder builder, CallbackInfoReturnable<?> cir)
	{
		Utils.ctxForSelector = ctx;
	}
}
