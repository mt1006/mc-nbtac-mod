package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Style;
import net.mt1006.nbtac.autocomplete.CustomTagParser;
import net.mt1006.nbtac.autocomplete.NbtSuggestionManager;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(StyleArgument.class)
public abstract class StyleArgumentMixin implements ArgumentType<Style>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			String tag = suggestionsBuilder.getRemaining();
			SuggestionList suggestionList = new SuggestionList();

			/*if (tag.isEmpty())
			{
				NbtSuggestionSubtype.getJsonTextPrefixSuggestions(suggestionList, false);
				return NbtSuggestionManager.finishSuggestions(suggestionList, suggestionsBuilder, null, 0);
			}*/

			Pair<CustomTagParser.Suggestion, Integer> results = CustomTagParser.parseJsonStyle(suggestionList, tag);
			return NbtSuggestionManager.finishSuggestions(suggestionList, suggestionsBuilder, results.getLeft(), results.getRight());
		}
		catch (Exception e)
		{
			return Suggestions.empty();
		}
	}
}
