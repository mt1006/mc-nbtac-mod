package com.mt1006.nbt_ac.autocomplete;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NbtSuggestions
{
	public static final Multimap<String, NbtSuggestion> prefixFullMap = HashMultimap.create();
	public static final Multimap<String, NbtSuggestion> suffixFullMap = HashMultimap.create();
	public static final Multimap<String, NbtSuggestion> fullMap = HashMultimap.create();
	private final Map<String, NbtSuggestion> suggestions = new HashMap<>();
	public final Multimap<String, NbtSuggestion> prefixMap = HashMultimap.create();
	public final Multimap<String, NbtSuggestion> suffixMap = HashMultimap.create();

	public void add(NbtSuggestion suggestion)
	{
		String key = suggestion.tag;
		NbtSuggestion oldVal = suggestions.put(key, suggestion);

		String prefix = key.substring(0, firstSeparator(key));
		String suffix = key.substring(lastSeparator(key));

		if (oldVal != null)
		{
			prefixMap.remove(prefix, oldVal);
			suffixMap.remove(suffix, oldVal);
			prefixFullMap.remove(prefix, oldVal);
			suffixFullMap.remove(suffix, oldVal);
			fullMap.remove(key, oldVal);
		}

		prefixMap.put(prefix, suggestion);
		suffixMap.put(suffix, suggestion);
		prefixFullMap.put(prefix, suggestion);
		suffixFullMap.put(suffix, suggestion);
		fullMap.put(key, suggestion);
	}

	public void copyAll(NbtSuggestions nbtSuggestions, boolean prediction)
	{
		nbtSuggestions.getAll().forEach((suggestion) -> add(suggestion.copy(prediction, nbtSuggestions, this)));
	}

	public NbtSuggestion get(String key)
	{
		return suggestions.get(key);
	}

	public Collection<NbtSuggestion> getAll()
	{
		return suggestions.values();
	}

	private static int firstSeparator(String str)
	{
		for (int i = 1; i < str.length(); i++)
		{
			char c = str.charAt(i);
			if (Character.isUpperCase(c) || c == '_') { return i; }
		}
		return str.length();
	}

	private static int lastSeparator(String str)
	{
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char c = str.charAt(i);
			if (Character.isUpperCase(c)) { return i; }
			else if (c == '_') { return i + 1; }
		}
		return 0;
	}
}
