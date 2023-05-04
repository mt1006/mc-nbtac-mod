package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;

public class SimpleSuggestion extends CustomSuggestion
{
	private final String text;
	private final String subtext;

	public SimpleSuggestion(String text, String subtext)
	{
		this.text = text;
		this.subtext = subtext;
	}

	@Override public String getSuggestionText()
	{
		return text;
	}

	@Override public String getSuggestionSubtext()
	{
		return subtext;
	}

	@Override public Message getSuggestionTooltip()
	{
		return null;
	}
}
