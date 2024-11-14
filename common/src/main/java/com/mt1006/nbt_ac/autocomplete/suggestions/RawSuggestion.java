package com.mt1006.nbt_ac.autocomplete.suggestions;

import org.jetbrains.annotations.Nullable;

public class RawSuggestion extends CustomSuggestion
{
	public RawSuggestion(String text, @Nullable String subtext, int priority)
	{
		super(text, subtext, priority);
	}

	public RawSuggestion(String text, @Nullable String subtext)
	{
		this(text, subtext, 0);
	}
}
