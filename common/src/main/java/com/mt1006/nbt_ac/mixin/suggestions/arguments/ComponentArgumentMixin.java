package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.SuggestionList;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestionSubtype;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(ComponentArgument.class)
public abstract class ComponentArgumentMixin implements ArgumentType<Component>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			String tag = suggestionsBuilder.getRemaining();
			SuggestionList suggestionList = new SuggestionList();

			if (tag.isEmpty())
			{
				NbtSuggestionSubtype.getJsonTextPrefixSuggestions(suggestionList, false);
				return NbtSuggestionManager.finishSuggestions(suggestionList, suggestionsBuilder, null, 0);
			}

			Pair<CustomTagParser.Suggestion, Integer> results = CustomTagParser.parseJsonComponent(suggestionList, tag, false);
			return NbtSuggestionManager.finishSuggestions(suggestionList, suggestionsBuilder, results.getLeft(), results.getRight());
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}
}
