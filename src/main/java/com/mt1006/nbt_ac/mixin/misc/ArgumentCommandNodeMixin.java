package com.mt1006.nbt_ac.mixin.misc;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.utils.Utils;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ArgumentCommandNode.class)
public class ArgumentCommandNodeMixin
{
	@Shadow(remap = false) @Final private SuggestionProvider<?> customSuggestions;
	@Shadow(remap = false) @Final private ArgumentType<?> type;
	@Unique private static int ccFixStatus = 0;

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

	/*
		Fix for crash when installed with Quilt, clientcommands and ViaFabricPlus (all at the same time),
		and some other mods (Animatica, Beautiflied Chat [Client] and Collective).

		clientcommand issue: https://github.com/Earthcomputer/clientcommands/issues/547
		NBT Autocomplete issue: https://github.com/mt1006/mc-nbtac-mod/issues/15
	 */
	@Inject(method = "isValidInput", at = @At(value = "HEAD"), cancellable = true, remap = false)
	private void atIsValidInput(String input, CallbackInfoReturnable<Boolean> cir)
	{
		if (ccFixStatus == 0)
		{
			boolean condition = Utils.isModPresent("quilt_loader")
					&& Utils.isModPresent("clientcommands") && Utils.isModPresent("viafabricplus");

			if (condition)
			{
				NBTac.LOGGER.warn("Applying q+cc+vfp fix");
				ccFixStatus = 1;
			}
			else
			{
				ccFixStatus = -1;
			}
		}

		if (ccFixStatus == 1)
		{
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
