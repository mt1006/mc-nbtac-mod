package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.MixinUtils;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(CompoundTagArgument.class)
abstract public class CompoundTagArgumentMixin implements ArgumentType<CompoundTag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			String name = getResourceName(commandContext);
			if (name == null) { return Suggestions.empty(); }

			String tag = suggestionsBuilder.getRemaining();

			return NbtSuggestionManager.loadFromName(name, tag, suggestionsBuilder, false);
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	private String getResourceName(CommandContext<?> commandContext)
	{
		String commandName = MixinUtils.getCommandName(commandContext);

		switch (commandName)
		{
			case "summon":
				EntityType<?> entityType = (EntityType<?>)commandContext.getArgument("entity", Holder.Reference.class).value();
				ResourceLocation resourceLocation = EntityType.getKey(entityType);
				return "entity/" + resourceLocation;

			case "data":
				return getResourceNameForDataCommand(commandContext);

			default:
				if (commandContext.getChild() != null) { return getResourceName(commandContext.getChild()); }
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
				Coordinates coords = commandContext.getArgument("targetPos", Coordinates.class);
				return MixinUtils.blockFromCoords(coords);

			case "entity":
				EntitySelector entitySelector = commandContext.getArgument("target", EntitySelector.class);
				return MixinUtils.entityFromEntitySelector(entitySelector);
		}
		return null;
	}
}
