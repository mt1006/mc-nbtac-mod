package com.mt1006.nbt_ac.autocomplete;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.RawSuggestion;
import com.mt1006.nbt_ac.utils.Fields;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NbtSuggestionManager
{
	private static final Map<String, NbtSuggestions> suggestionMap = new HashMap<>();
	public static final Map<Suggestion, CustomSuggestion.Data> dataMap = new IdentityHashMap<>();
	public static boolean hasCustomSuggestions = false;
	private static @Nullable SuggestionsBuilder oldBuilder = null;
	private static @Nullable List<Suggestion> oldSuggestionList = null;
	private static int suggestionListCounter = 0;

	public static void add(String key, NbtSuggestions suggestions)
	{
		suggestionMap.put(key, suggestions);
	}

	public static @Nullable NbtSuggestions get(@Nullable String key)
	{
		//TODO: check if null check is necessary (1.4)
		if (key == null) { return null; }
		return suggestionMap.get(key);
	}

	public static Set<Map.Entry<String, NbtSuggestions>> suggestionSet()
	{
		return suggestionMap.entrySet();
	}

	public static CompletableFuture<Suggestions> loadFromName(String name, String tag, SuggestionsBuilder suggestionsBuilder, boolean suggestPath)
	{
		return load(get(name), tag, suggestionsBuilder, suggestPath, name, null);
	}

	public static CompletableFuture<Suggestions> loadFromSuggestion(NbtSuggestion suggestion, String tag, SuggestionsBuilder suggestionsBuilder)
	{
		return load(suggestion.subcompound, tag, suggestionsBuilder, false, null, suggestion);
	}

	public static CompletableFuture<Suggestions> load(@Nullable NbtSuggestions suggestions, String tag, SuggestionsBuilder suggestionsBuilder,
													  boolean suggestPath, @Nullable String rootTag, @Nullable NbtSuggestion rootSuggestion)
	{
		if (!Loader.finished)
		{
			NbtSuggestionManager.simpleSuggestion("", "[suggestions not loaded]", suggestionsBuilder);
			return suggestionsBuilder.buildFuture();
		}

		String rootName = rootTag != null ? rootTag : (rootSuggestion != null ? rootSuggestion.tag : null);

		CustomTagParser tagParser = new CustomTagParser(tag, suggestPath ? CustomTagParser.Type.PATH : CustomTagParser.Type.COMPOUND);
		SuggestionList suggestionList = tagParser.prepareSuggestionList(suggestions, rootTag);
		CustomTagParser.Suggestion suggestionToShow = tagParser.read(suggestionList, rootSuggestion, rootName);

		return finishSuggestions(suggestionList, suggestionsBuilder, suggestionToShow, tagParser.getCursor());
	}

	public static CompletableFuture<Suggestions> finishSuggestions(SuggestionList suggestionList, SuggestionsBuilder suggestionsBuilder,
																   @Nullable CustomTagParser.Suggestion suggestionToShow, int cursor)
	{
		int maxOffset = suggestionsBuilder.getInput().length();
		int newOffset = suggestionsBuilder.getStart() + cursor;
		if (newOffset > maxOffset)
		{
			SuggestionsBuilder errorBuilder = suggestionsBuilder.createOffset(maxOffset);
			errorBuilder.suggest("_error");
			return errorBuilder.buildFuture();
		}

		SuggestionsBuilder newSuggestionsBuilder = suggestionsBuilder.createOffset(newOffset);

		if (suggestionToShow == null || suggestionToShow == CustomTagParser.Suggestion.TAG)
		{
			suggestionList.forEach((s) -> s.suggest(newSuggestionsBuilder));
		}
		else
		{
			suggestionToShow.suggest(newSuggestionsBuilder);
		}

		return newSuggestionsBuilder.buildFuture();
	}

	public static void simpleSuggestion(String text, String subtext, SuggestionsBuilder suggestionsBuilder)
	{
		new RawSuggestion(text, subtext).suggest(suggestionsBuilder);
	}

	public static @Nullable String getSubtext(Suggestion suggestion)
	{
		CustomSuggestion.Data data = dataMap.get(suggestion);
		return data != null ? data.subtext : null;
	}

	public static void clearProvided()
	{
		dataMap.clear();
		hasCustomSuggestions = false;
		suggestionListCounter = 0;
	}

	public static void clearIfNeeded(SuggestionsBuilder builder)
	{
		// prevents memory leak on Forge and NeoForge
		if (Fields.suggestionsBuilderList == null) { return; }

		try
		{
			List<Suggestion> suggestionList = (List<Suggestion>)Fields.suggestionsBuilderList.get(builder);
			if (oldBuilder != null && (!builder.getInput().equals(oldBuilder.getInput())
					|| builder.getStart() != oldBuilder.getStart()))
			{
				clearProvided();
			}
			else if (suggestionList != oldSuggestionList)
			{
				/*
					This counter is used to prevent memory leak when refreshing suggestions with right arrow.
					It may potentially cause some issues when suggestions are queried multiple times for single list;
					in such cases using right arrow may cause additional data to be removed.
					6 is used to prevent this from happening in case of "/tp @p[nbt={" which queries 2 times
					and should also prevent this in case of suggestions being queried 3 or 6 times.
				*/

				suggestionListCounter++;
				if (suggestionListCounter >= 6) { clearProvided(); }
			}

			oldBuilder = builder;
			oldSuggestionList = suggestionList;
		}
		catch (Exception ignore) {}
	}
}
