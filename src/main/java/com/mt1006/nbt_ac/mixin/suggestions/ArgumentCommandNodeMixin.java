package com.mt1006.nbt_ac.mixin.suggestions;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ArgumentCommandNode.class)
public class ArgumentCommandNodeMixin
{
	@Shadow(remap = false) @Final private SuggestionProvider<?> customSuggestions;
	@Shadow(remap = false) @Final private ArgumentType<?> type;

	// Fix for conflict with the Polymer
	@Inject(method = "listSuggestions", at = @At(value = "HEAD"), cancellable = true, remap = false)
	private void listSuggestions(CommandContext<?> context, SuggestionsBuilder builder,
								 CallbackInfoReturnable<CompletableFuture<Suggestions>> cir)
	{
		if (customSuggestions != null && builder.getRemaining().contains("{") &&
				(type instanceof ItemArgument || type instanceof BlockStateArgument || type instanceof ResourceArgument<?>))
		{
			cir.setReturnValue(type.listSuggestions(context, builder));
			cir.cancel();
		}
	}
}
