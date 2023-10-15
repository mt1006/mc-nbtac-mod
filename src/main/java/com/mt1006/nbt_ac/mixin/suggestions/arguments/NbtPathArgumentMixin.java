package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.MixinUtils;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(NBTPathArgument.class)
abstract public class NbtPathArgumentMixin implements ArgumentType<CompoundNBT>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			String name = getResourceName(commandContext, suggestionsBuilder.getStart());
			if (name == null) { return Suggestions.empty(); }

			String tag = suggestionsBuilder.getRemaining();

			return NbtSuggestionManager.loadFromName(name, tag, suggestionsBuilder, true);
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	private @Nullable String getResourceName(CommandContext<?> commandContext, int cursor)
	{
		if (commandContext.getRange().getEnd() < cursor && commandContext.getChild() != null)
		{
			return getResourceName(commandContext.getChild(), cursor);
		}

		String commandName = MixinUtils.getCommandName(commandContext);
		boolean isExecuteCommand = commandName.equals("execute");

		if (commandContext.getRootNode() instanceof RootCommandNode<?> && commandName.equals("data"))
		{
			return getResourceNameForDataCommand(commandContext);
		}
		else if (isExecuteCommand || commandContext.getRootNode().getName().equals("execute"))
		{
			if (isExecuteCommand) { commandName = MixinUtils.getNodeString(commandContext, 1); }

			if (commandName.equals("if") || commandName.equals("unless"))
			{
				return getResourceNameForExecuteCommand(commandContext, true, isExecuteCommand);
			}
			else if (commandName.equals("store"))
			{
				return getResourceNameForExecuteCommand(commandContext, false, isExecuteCommand);
			}
		}
		return null;
	}

	private @Nullable String getResourceNameForDataCommand(CommandContext<?> commandContext)
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

		return getResourceNameForArguments(commandContext, type, blockArgument, entityArgument);
	}

	private @Nullable String getResourceNameForExecuteCommand(CommandContext<?> commandContext, boolean isIf, boolean withOffset)
	{
		int offset = withOffset ? 1 : 0;
		if (isIf && !MixinUtils.getNodeString(commandContext, 1 + offset).equals("data")) { return null; }

		String type = MixinUtils.getNodeString(commandContext, 2 + offset);
		return getResourceNameForArguments(commandContext, type, isIf ? "sourcePos" : "targetPos", isIf ? "source" : "target");
	}

	private String getResourceNameForArguments(CommandContext<?> commandContext, String type, String blockArgument, String entityArgument)
	{
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
