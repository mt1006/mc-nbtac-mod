package com.mt1006.nbt_ac.autocomplete;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.SimpleSuggestion;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NbtSuggestionManager
{
	public static final Map<String, NbtSuggestions> suggestionMap = new HashMap<>();
	public static final Map<Suggestion, String> subtextMap = new IdentityHashMap<>();

	public static void add(String key, NbtSuggestions suggestions)
	{
		suggestionMap.put(key, suggestions);
	}

	public static NbtSuggestions get(String key)
	{
		if (key == null) { return null; }
		return suggestionMap.get(key);
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
		if (subtextMap.size() > 1024) { subtextMap.clear(); }

		if (!Loader.finished.get())
		{
			NbtSuggestionManager.simpleSuggestion("", "§8[suggestions not loaded]", suggestionsBuilder);
			return suggestionsBuilder.buildFuture();
		}

		String rootName = rootTag != null ? rootTag : (rootSuggestion != null ? rootSuggestion.tag : null);

		List<CustomSuggestion> suggestionList = new ArrayList<>();
		addToList(suggestionList, suggestions, rootTag);

		CustomTagParser customTagParser = new CustomTagParser(tag);
		CustomTagParser.Suggestion suggestionToShow = customTagParser.read(suggestionList, rootSuggestion, rootName, suggestPath);

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

		return newSuggestionsBuilder.buildFuture();
	}

	public static void addToList(List<CustomSuggestion> suggestionList, @Nullable NbtSuggestions suggestions, @Nullable String rootTag)
	{
		if (suggestions != null) { suggestionList.addAll(suggestions.getAll()); }

		for (NbtSuggestions commonSuggestions : getCommonSuggestions(rootTag))
		{
			if (commonSuggestions != null) { suggestionList.addAll(commonSuggestions.getAll()); }
		}
	}

	public static void simpleSuggestion(String text, String subtext, SuggestionsBuilder suggestionsBuilder)
	{
		new SimpleSuggestion(text, subtext).suggest(suggestionsBuilder, subtextMap);
	}

	public static String getSubtext(Suggestion suggestion)
	{
		return subtextMap.get(suggestion);
	}

	private static List<@Nullable NbtSuggestions> getCommonSuggestions(@Nullable String tag)
	{
		if (tag == null) { return Collections.emptyList(); }
		List<NbtSuggestions> list = new ArrayList<>();

		if (tag.startsWith("item/"))
		{
			Item item = RegistryUtils.ITEM.get(tag.substring(5));
			if (item != null)
			{
				if (item instanceof BlockItem) { list.add(get("common/block_item")); }
				if (item instanceof SpawnEggItem) { list.add(get("common/spawn_egg_item")); }
				if (item.canBeDepleted()) { list.add(get("common/damageable")); }
			}
			list.add(get("common/item"));
		}
		else if (tag.startsWith("block/"))
		{
			list.add(get("common/block"));
		}
		else if (tag.startsWith("entity/")) {
			list.add(get("common/entity"));
		}
		return list;
	}
}
