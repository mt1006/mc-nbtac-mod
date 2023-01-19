package com.mt1006.nbt_ac.autocomplete;

import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;

import java.util.ArrayList;

public class NbtSuggestions
{
	public ArrayList<NbtSuggestion> suggestions = new ArrayList<>();

	public void addSuggestion(NbtSuggestion suggestion)
	{
		suggestions.removeIf(listElement -> listElement.tag.equals(suggestion.tag));
		suggestions.add(suggestion);
	}
}
