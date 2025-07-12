package net.mt1006.nbtac.autocomplete;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.loader.Loader;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.RawSuggestion;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Fields;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SuggestionManager
{
	public static final Map<Suggestion, CustomSuggestion.Data> dataMap = new IdentityHashMap<>();
	public static boolean hasCustomSuggestions = false;

	// variables used on Forge and NeoForge
	private static @Nullable SuggestionsBuilder oldBuilder = null;
	private static @Nullable List<Suggestion> oldSuggestionList = null;
	private static int suggestionListCounter = 0;

	public static CompletableFuture<Suggestions> loadFromName(String str, @Nullable String name, SuggestionsBuilder builder, boolean suggestPath)
	{
		if (!Loader.finished)
		{
			new RawSuggestion("", "[suggestions not loaded]").suggest(builder);
			return builder.buildFuture();
		}

		if (name == null) { return Suggestions.empty(); }
		NbtTagMap tagMap = NbtTagManager.get(name);

		CustomTagParser parser = suggestPath
				? CustomTagParser.forNbtPath(str, tagMap)
				: CustomTagParser.forNbtCompound(str, tagMap);
		return finishSuggestions(parser::parse, builder);
	}

	public static CompletableFuture<Suggestions> loadFromType(String str, Type type, SuggestionsBuilder builder)
	{
		if (!Loader.finished)
		{
			new RawSuggestion("", "[suggestions not loaded]").suggest(builder);
			return builder.buildFuture();
		}

		CustomTagParser parser = CustomTagParser.forValueOfType(str, type);
		return finishSuggestions(parser::parse, builder);
	}

	public static CompletableFuture<Suggestions> finishSuggestions(Supplier<SuggestionList> supplier, SuggestionsBuilder builder)
	{
		SuggestionList list;
		try
		{
			list = supplier.get();
		}
		catch (Exception e)
		{
			if (ModConfig.debugMode.val) { e.printStackTrace(); }
			return Suggestions.empty();
		}
		
		int maxOffset = builder.getInput().length();
		int newOffset = builder.getStart() + list.cursor;
		if (newOffset > maxOffset)
		{
			SuggestionsBuilder errorBuilder = builder.createOffset(maxOffset);
			errorBuilder.suggest("error");
			return errorBuilder.buildFuture();
		}

		SuggestionsBuilder newBuilder = builder.createOffset(newOffset);
		list.forEach((s) -> s.suggest(newBuilder));
		return newBuilder.buildFuture();
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
		if (NBTac.loaderInterface.isFabric() || Fields.suggestionsBuilderList == null) { return; }

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
