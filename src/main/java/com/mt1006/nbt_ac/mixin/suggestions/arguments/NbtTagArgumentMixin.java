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
import com.mt1006.nbt_ac.utils.Utils;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(NbtTagArgument.class)
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

	@Unique private NbtSuggestion getSuggestion(CommandContext<?> ctx)
	{
		String commandName = Utils.getCommandName(ctx);
		if (commandName.equals("data")) { return getSuggestionForDataCommand(ctx); }
		else if (ctx.getChild() != null) { return getSuggestion(ctx.getChild()); }
		return null;
	}

	@Unique private NbtSuggestion getSuggestionForDataCommand(CommandContext<?> ctx)
	{
		String instruction = Utils.getNodeString(ctx, 1);
		if (!instruction.equals("modify")) { return null; }

		String type = Utils.getNodeString(ctx, 2);
		String path = Utils.getArgumentString(ctx, "targetPath");

		String root;

		switch (type)
		{
			case "block":
				Coordinates coords = ctx.getArgument("targetPos", Coordinates.class);
				root =  Utils.blockFromCoords(coords);
				break;

			case "entity":
				EntitySelector entitySelector = ctx.getArgument("target", EntitySelector.class);
				root = Utils.entityFromEntitySelector(entitySelector);
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
