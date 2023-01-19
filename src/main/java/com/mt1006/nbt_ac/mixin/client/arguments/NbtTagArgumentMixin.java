package com.mt1006.nbt_ac.mixin.client.arguments;

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
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(NbtTagArgument.class)
abstract public class NbtTagArgumentMixin implements ArgumentType<Tag>
{
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			NbtSuggestion nbtSuggestion = getSuggestion(commandContext);
			if (nbtSuggestion == null ) { return Suggestions.empty(); }

			String tag = suggestionsBuilder.getRemaining();

			return NbtSuggestionManager.loadSuggestions(nbtSuggestion.subcompound,
					nbtSuggestion.getComplexTag(), tag, suggestionsBuilder, false).buildFuture();
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	private NbtSuggestion getSuggestion(CommandContext<?> commandContext)
	{
		String commandName = MixinUtils.getNodeString(commandContext, 0);
		if (commandName.equals("data")) { return getSuggestionForDataCommand(commandContext); }
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
				Coordinates coords = commandContext.getArgument("targetPos", Coordinates.class);
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

		List<CustomSuggestion> suggestionList = new LinkedList<>(NbtSuggestionManager.get(root).suggestions);

		CustomTagParser pathParser = new CustomTagParser(path);
		pathParser.read(suggestionList, "$tag/" + root, true);

		return pathParser.lastFoundSuggestion;
	}
}
