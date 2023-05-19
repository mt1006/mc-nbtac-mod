package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.types.templates.Tag;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.utils.MixinUtils;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.NBTTagArgument;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(NBTTagArgument.class)
abstract public class NbtTagArgumentMixin implements ArgumentType<Tag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			NbtSuggestion nbtSuggestion = getSuggestion(commandContext);
			if (nbtSuggestion == null) { return Suggestions.empty(); }

			String tag = suggestionsBuilder.getRemaining();

			if (nbtSuggestion.subcompound == null)
			{
				NbtSuggestionManager.simpleSuggestion("", String.format("§8%s[%s]",
						nbtSuggestion.suggestionType.symbol, nbtSuggestion.type.getName()), suggestionsBuilder);
				return suggestionsBuilder.buildFuture();
			}
			else
			{
				return NbtSuggestionManager.loadFromSuggestion(nbtSuggestion, tag, suggestionsBuilder);
			}
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	private NbtSuggestion getSuggestion(CommandContext<?> commandContext)
	{
		String commandName = MixinUtils.getCommandName(commandContext);
		if (commandName.equals("data")) { return getSuggestionForDataCommand(commandContext); }
		else if (commandContext.getChild() != null) { return getSuggestion(commandContext.getChild()); }
		return null;
	}

	private NbtSuggestion getSuggestionForDataCommand(CommandContext<?> commandContext)
	{
		String instruction = MixinUtils.getNodeString(commandContext, 1);
		if (!instruction.equals("modify")) { return null; }

		String type = MixinUtils.getNodeString(commandContext, 2);
		String path = MixinUtils.getArgumentString(commandContext, "targetPath");

		String root;

		switch (type)
		{
			case "block":
				ILocationArgument coords = commandContext.getArgument("targetPos", ILocationArgument.class);
				root =  MixinUtils.blockFromCoords(coords);
				break;

			case "entity":
				EntitySelector entitySelector = commandContext.getArgument("target", EntitySelector.class);
				root = MixinUtils.entityFromEntitySelector(entitySelector);
				break;

			default:
				return null;
		}

		if (root == null) { return null; }

		NbtSuggestions rootSuggestions = NbtSuggestionManager.get(root);
		if (rootSuggestions == null) { return null; }

		List<CustomSuggestion> suggestionList = new ArrayList<>();
		NbtSuggestionManager.addToList(suggestionList, rootSuggestions, root);

		CustomTagParser pathParser = new CustomTagParser(path);
		pathParser.read(suggestionList, null, root, true);

		return pathParser.lastFoundSuggestion;
	}
}
