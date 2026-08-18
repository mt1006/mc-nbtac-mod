package net.mt1006.nbtac.api.v1;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.DataComponentManager;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.loader.SuggestionDataParser;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.tag.DefinedNbtTag;
import net.mt1006.nbtac.autocomplete.type.compound.CompoundType;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NBTacAPI
{
	public static final int API_VERSION_MAJOR = 1;
	public static final int API_VERSION_MINOR = 0;

	public static void addCustomSuggestions(String group, String namespace, String contents, boolean dataComponents)
	{
		if (namespace.equals("minecraft") || namespace.equals("nbtac"))
		{
			throw new IllegalArgumentException("Extending existing extensions not supported");
		}

		try
		{
			SuggestionDataParser parser = new SuggestionDataParser(group, namespace, contents);
			if (dataComponents) { parser.parseDataComponents(); }
			else { parser.parseNbtSuggestions(); }
		}
		catch (Exception e)
		{
			NBTac.LOGGER.warn("Failed to load suggestions {}/{}", group, namespace);
		}
	}

	public static CompletableFuture<Suggestions> getNbtSuggestions(String input, String name, @Nullable SuggestionsBuilder builder, boolean suggestPath,
	                                                               @Nullable Function<NBTacSuggestionList, NBTacSuggestionList> process)
	{
		if (builder == null) { builder = new SuggestionsBuilder("", 0); }
		return SuggestionManager.get(input, CompoundType.fromName(name), builder, suggestPath, (sl) -> processSuggestions(process, sl));
	}

	public static CompletableFuture<Suggestions> getItemDataSuggestions(String input, String name, @Nullable Identifier itemId, @Nullable SuggestionsBuilder builder,
	                                                                    @Nullable Function<NBTacSuggestionList, NBTacSuggestionList> process)
	{
		if (builder == null) { builder = new SuggestionsBuilder("", 0); }

		DefinedNbtTag tag = DataComponentManager.componentMap.get(name);
		if (tag == null) { return Suggestions.empty(); }

		CustomTagParser parser = CustomTagParser.forDataComponentValue(input, tag.getType(), itemId);
		return SuggestionManager.finishSuggestions(parser::parse, builder, (sl) -> processSuggestions(process, sl));
	}

	private static SuggestionList processSuggestions(@Nullable Function<NBTacSuggestionList, NBTacSuggestionList> process, SuggestionList list)
	{
		if (process == null) { return list; }

		List<NBTacSuggestion> suggestions = new LinkedList<>();
		list.forEach(suggestions::add);
		NBTacSuggestionList apiList = process.apply(new NBTacSuggestionList(suggestions, list.cursor));

		SuggestionList newList = new SuggestionList(apiList.cursor());
		apiList.suggestions().forEach((s) -> newList.add(s.get()));
		return newList;
	}
}
