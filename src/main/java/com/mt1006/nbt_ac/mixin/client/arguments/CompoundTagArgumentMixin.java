package com.mt1006.nbt_ac.mixin.client.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.MixinUtils;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(NBTCompoundTagArgument.class)
abstract public class CompoundTagArgumentMixin implements ArgumentType<CompoundNBT>
{
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			String name = getResourceName(commandContext);
			if (name == null) { return Suggestions.empty(); }

			String tag = suggestionsBuilder.getRemaining();

			return NbtSuggestionManager.loadSuggestions(NbtSuggestionManager.get(name),
					"$tag/" + name, tag, suggestionsBuilder, false).buildFuture();
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	private String getResourceName(CommandContext<?> commandContext)
	{
		String commandName = MixinUtils.getNodeString(commandContext, 0);

		switch (commandName)
		{
			case "summon":
				ResourceLocation resourceLocation = commandContext.getArgument("entity", ResourceLocation.class);
				return "entity/" + resourceLocation;

			case "data":
				return getResourceNameForDataCommand(commandContext);
		}

		return null;
	}

	private String getResourceNameForDataCommand(CommandContext<?> commandContext)
	{
		String instruction = MixinUtils.getNodeString(commandContext, 1);
		if (!instruction.equals("merge")) { return null; }
		String targetType = MixinUtils.getNodeString(commandContext, 2);

		switch (targetType)
		{
			case "block":
				ILocationArgument coords = commandContext.getArgument("targetPos", ILocationArgument.class);
				return MixinUtils.blockFromCoords(coords);

			case "entity":
				EntitySelector entitySelector = commandContext.getArgument("target", EntitySelector.class);
				return MixinUtils.entityFromEntitySelector(entitySelector);
		}

		return null;
	}
}
