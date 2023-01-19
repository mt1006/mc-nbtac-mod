package com.mt1006.nbt_ac.mixin.client.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.MixinUtils;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(NBTPathArgument.class)
abstract public class NbtPathArgumentMixin implements ArgumentType<CompoundNBT>
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
					"$tag/" + name, tag, suggestionsBuilder, true).buildFuture();
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	private String getResourceName(CommandContext<?> commandContext)
	{
		String commandName = MixinUtils.getNodeString(commandContext, 0);
		if (commandName.equals("data")) { return getResourceNameForDataCommand(commandContext); }
		return null;
	}

	private String getResourceNameForDataCommand(CommandContext<?> commandContext)
	{
		String blockArgument = "targetPos";
		String entityArgument = "target";
		String instruction = MixinUtils.getNodeString(commandContext, 1);
		String type = MixinUtils.getNodeString(commandContext, 2);

		switch (instruction)
		{
			case "get":
			case "remove":
				break;

			case "modify":
				if (commandContext.getNodes().size() > 7)
				{
					String modification = MixinUtils.getNodeString(commandContext, 5);

					if (modification.equals("insert")) { type = MixinUtils.getNodeString(commandContext, 8); }
					else { type = MixinUtils.getNodeString(commandContext, 7); }

					blockArgument = "sourcePos";
					entityArgument = "source";
				}
				break;

			default:
				return null;
		}

		switch (type)
		{
			case "block":
				ILocationArgument coords = commandContext.getArgument(blockArgument, ILocationArgument.class);
				return MixinUtils.blockFromCoords(coords);

			case "entity":
				EntitySelector entitySelector = commandContext.getArgument(entityArgument, EntitySelector.class);
				return MixinUtils.entityFromEntitySelector(entitySelector);
		}

		return null;
	}
}
