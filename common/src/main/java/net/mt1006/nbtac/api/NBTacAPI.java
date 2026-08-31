package net.mt1006.nbtac.api;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.DataComponentManager;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.loader.SuggestionDataParser;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.tag.DefinedNbtTag;
import net.mt1006.nbtac.autocomplete.type.compound.CompoundType;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NBTacAPI
{
	// these values are for suggestion data format / available suggestion types
	// if you don't use addCustomSuggestions(), you don't need to care about it
	// major version is for marking changes breaking backwards compatibility
	// minor version is for marking any changes that are not forward compatible
	public static final int SUGGESTION_FORMAT_VERSION_MAJOR = 1;
	public static final int SUGGESTION_FORMAT_VERSION_MINOR = 0;

	/**
	 * Adds custom suggestions. It parses suggestions using same format as built-in suggestion files.
	 *
	 * <p>
	 * Example:
	 * <pre>
	 * {@code
	 * String contents = """
	 * # comment
	 * my_entity &:_living_entity
	 * +field1 :int
	 * +field2 :compound
	 * 	+subfield :TextCompound
	 *
	 * my_another_entity =:_mob
	 * """;
	 *
	 * NBTacAPI.addCustomSuggestions("entity", "mymod", contents, false);
	 * }
	 * </pre>
	 *
	 * Suggestions for data components are a bit different, see built-in item suggestions.
	 * For other examples from build-in suggestions, see resources/suggestions_v3.
	 * <p>
	 * Note that built-in suggestions don't use &:id, but &id for minecraft:id, as they're in "minecraft" namespace.
	 * If colon is not present it will always use namespace given as argument.
	 * If you want to use "minecraft" namespace in your suggestions, you need to use &:id.
	 * If you want to use namespace other than given and "minecraft", you need to use colon twice, e.g. &:othermod:id.
	 * <p>
	 * You should call this method during game initialization. It should be safe to call this method from any thread.
	 * You should not try to override existing suggestions. It's also recommended to add all suggestions for given
	 * group and namespace in single call, so don't call it for each entity separately.
	 *
	 * @param group group of suggestions, e.g. block, entity, item
	 * @param namespace namespace of suggestion entries
	 * @param contents contents to parse, see example above
	 * @param dataComponents true for data component suggestions, false for normal NBT suggestions
	 */
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
			e.printStackTrace();
		}
	}

	/**
	 * Get suggestions for NBT compound value or path.
	 * @param input argument part to get suggestions for, e.g. "{IsBaby:true,act"
	 * @param name name of a suggestion - "group/namespace:id", e.g. "entity/minecraft:creeper"
	 * @param builder suggestion builder, if null dummy builder will be created
	 * @param suggestPath whenever to provide suggestions for path, e.g. for "active_effects[0]."
	 * @param process if not null, it can be used to modify suggestion list
	 * @return suggestions
	 */
	public static CompletableFuture<Suggestions> getNbtSuggestions(String input, String name, @Nullable SuggestionsBuilder builder, boolean suggestPath,
	                                                               @Nullable Function<NBTacSuggestionList, NBTacSuggestionList> process)
	{
		if (builder == null) { builder = new SuggestionsBuilder(input, 0); }
		return SuggestionManager.get(input, CompoundType.fromName(name), builder, suggestPath, (sl) -> processSuggestions(process, sl));
	}

	/**
	 * Get suggestions for item data components. On versions prior to 1.20.5, use getNbtSuggestions() instead.
	 * This method provides suggestions only for data component values, not for entire component list.
	 * For example, for "egg[enchantments={" you need to give "{" as input, and "item/minecraft:enchantments" as name.
	 *
	 * @param input data component value part to get suggestions for, e.g. "tru"
	 * @param name name of a data component prefixed by "item/", e.g. "item/minecraft:map_decoration"
	 * @param itemId optional item ID (might be null)
	 * @param builder suggestion builder, if null dummy builder will be created
	 * @param process if not null, it can be used to modify suggestion list
	 * @return suggestions
	 */
	public static CompletableFuture<Suggestions> getItemDataSuggestions(String input, String name, @Nullable ResourceLocation itemId, @Nullable SuggestionsBuilder builder,
	                                                                    @Nullable Function<NBTacSuggestionList, NBTacSuggestionList> process)
	{
		if (builder == null) { builder = new SuggestionsBuilder(input, 0); }

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
