package net.mt1006.nbtac.autocomplete;

import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import net.mt1006.nbtac.autocomplete.suggestions.TagSuggestion;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbtTagMap
{
	private @Nullable Map<String, NbtTag> map = null;
	private boolean containsIdTag = false;

	public boolean add(NbtTag tag)
	{
		if (map == null) { map = new HashMap<>(); }
		if (tag.getNameAsId() != null) { containsIdTag = true; }
		return (map.put(tag.getName(), tag) == null);
	}

	public void addAll(@Nullable NbtTagMap tagMap)
	{
		if (tagMap != null) { tagMap.getAll().forEach(this::add); }
	}

	public @Nullable NbtTag get(String key)
	{
		if (map == null) { return null; }

		if (containsIdTag)
		{
			NbtTag idTag = map.get("minecraft:" + key);
			if (idTag != null) { return idTag; }
		}
		return map.get(key);
	}

	public boolean containsKey(String key)
	{
		return get(key) != null;
	}

	public Collection<NbtTag> getAll()
	{
		return map != null ? map.values() : List.of();
	}

	public SuggestionList suggestionsForKeyPrefix(ParserType parserType, ParsedCompound compound, String prefix, int cursorPos)
	{
		SuggestionList list = new SuggestionList(cursorPos);
		getAll().forEach((tag) -> list.add(TagSuggestion.create(tag, parserType, compound)));

		list.filterByPrefix(prefix);
		compound.getAll().forEach((t) -> list.removeByName(t.key));
		return list;
	}
}
