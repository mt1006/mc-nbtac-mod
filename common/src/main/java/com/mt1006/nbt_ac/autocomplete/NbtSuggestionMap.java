package com.mt1006.nbt_ac.autocomplete;

import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbtSuggestionMap
{
	private @Nullable Map<String, NbtSuggestion> map = null;

	public boolean add(NbtSuggestion suggestion)
	{
		if (map == null) { map = new HashMap<>(); }
		return (map.put(suggestion.tag, suggestion) == null);
	}

	//TODO: remove?
	/*public void copyAll(NbtSuggestionMap nbtSuggestions)
	{
		nbtSuggestions.getAll().forEach(this::add);
	}*/

	public @Nullable NbtSuggestion get(String key)
	{
		return map != null ? map.get(key) : null;
	}

	public Collection<NbtSuggestion> getAll()
	{
		return map != null ? map.values() : List.of();
	}
}
