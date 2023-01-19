package com.mt1006.nbt_ac.autocomplete;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.SimpleSuggestion;

import java.util.*;

public class NbtSuggestionManager
{
	private static final Map<String, NbtSuggestions> suggestionMap = new HashMap<>();
	private static final Map<Suggestion, String> subtextMap = new IdentityHashMap<>();

	public static void add(String key, NbtSuggestions suggestions)
	{
		suggestionMap.put(key, suggestions);
	}

	public static NbtSuggestions get(String key)
	{
		if (key == null) { return null; }
		return suggestionMap.get(key);
	}

	public static SuggestionsBuilder loadSuggestions(NbtSuggestions suggestions, String parentComplexTag, String tag,
													 SuggestionsBuilder suggestionsBuilder, boolean suggestPath)
	{
		subtextMap.clear();

		if (!Loader.finished.get())
		{
			(new SimpleSuggestion("", "§8[suggestions not loaded]")).suggest(suggestionsBuilder, subtextMap);
			return suggestionsBuilder;
		}

		List<CustomSuggestion> suggestionList = new LinkedList<>();
		if (suggestions != null) { suggestionList.addAll(suggestions.suggestions); }

		CustomTagParser customTagParser = new CustomTagParser(tag);
		CustomTagParser.Suggestion suggestionToShow = customTagParser.read(suggestionList, parentComplexTag, suggestPath);

		SuggestionsBuilder newSuggestionsBuilder = suggestionsBuilder.createOffset(suggestionsBuilder.getStart() + customTagParser.reader.getCursor());

		if (suggestionToShow == CustomTagParser.Suggestion.TAG)
		{
			for (CustomSuggestion suggestion : suggestionList)
			{
				suggestion.suggest(newSuggestionsBuilder, subtextMap);
			}
		}
		else
		{
			suggestionToShow.suggest(newSuggestionsBuilder);
		}

		return newSuggestionsBuilder;
	}

	public static String getSubtext(Suggestion suggestion)
	{
		return subtextMap.get(suggestion);
	}

	public static Map<String, NbtSuggestions> getSuggestionMap() { return suggestionMap; }

	public static Map<Suggestion, String> getSubtextMap()
	{
		return subtextMap;
	}
}
