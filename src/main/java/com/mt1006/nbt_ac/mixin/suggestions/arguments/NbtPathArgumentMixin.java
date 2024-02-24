package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.Utils;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(NbtPathArgument.class)
abstract public class NbtPathArgumentMixin implements ArgumentType<CompoundTag>
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

	@Unique private @Nullable String getResourceName(CommandContext<?> ctx, int cursor)
	{
		if (ctx.getRange().getEnd() < cursor && ctx.getChild() != null)
		{
			return getResourceName(ctx.getChild(), cursor);
		}

		String commandName = Utils.getCommandName(ctx);
		boolean isExecuteCommand = commandName.equals("execute");

		if (ctx.getRootNode() instanceof RootCommandNode<?> && commandName.equals("data"))
		{
			return getResourceNameForDataCommand(ctx);
		}
		else if (isExecuteCommand || ctx.getRootNode().getName().equals("execute"))
		{
			if (isExecuteCommand) { commandName = Utils.getNodeString(ctx, 1); }

			if (commandName.equals("if") || commandName.equals("unless"))
			{
				return getResourceNameForExecuteCommand(ctx, true, isExecuteCommand);
			}
			else if (commandName.equals("store"))
			{
				return getResourceNameForExecuteCommand(ctx, false, isExecuteCommand);
			}
		}
		return null;
	}

	@Unique private @Nullable String getResourceNameForDataCommand(CommandContext<?> ctx)
	{
		String blockArgument = "targetPos";
		String entityArgument = "target";
		String instruction = Utils.getNodeString(ctx, 1);
		String type = Utils.getNodeString(ctx, 2);

		switch (instruction)
		{
			case "get":
			case "remove":
				break;

			case "modify":
				if (ctx.getNodes().size() > 7)
				{
					String modification = Utils.getNodeString(ctx, 5);

					if (modification.equals("insert")) { type = Utils.getNodeString(ctx, 8); }
					else { type = Utils.getNodeString(ctx, 7); }

					blockArgument = "sourcePos";
					entityArgument = "source";
				}
				break;

			default:
				return null;
		}

		return getResourceNameForArguments(ctx, type, blockArgument, entityArgument);
	}

	@Unique private @Nullable String getResourceNameForExecuteCommand(CommandContext<?> ctx, boolean isIf, boolean withOffset)
	{
		int offset = withOffset ? 1 : 0;
		if (isIf && !Utils.getNodeString(ctx, 1 + offset).equals("data")) { return null; }

		String type = Utils.getNodeString(ctx, 2 + offset);
		return getResourceNameForArguments(ctx, type, isIf ? "sourcePos" : "targetPos", isIf ? "source" : "target");
	}

	@Unique private String getResourceNameForArguments(CommandContext<?> ctx, String type, String blockArgument, String entityArgument)
	{
		switch (type)
		{
			case "block":
				Coordinates coords = ctx.getArgument(blockArgument, Coordinates.class);
				return Utils.blockFromCoords(coords);

			case "entity":
				EntitySelector entitySelector = ctx.getArgument(entityArgument, EntitySelector.class);
				return Utils.entityFromEntitySelector(entitySelector);
		}
		return null;
	}
}
