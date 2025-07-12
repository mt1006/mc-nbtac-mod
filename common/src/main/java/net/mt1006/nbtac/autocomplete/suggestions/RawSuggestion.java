package net.mt1006.nbtac.autocomplete.suggestions;

import org.jetbrains.annotations.Nullable;

public class RawSuggestion extends CustomSuggestion
{
	public final String text;

	public RawSuggestion(String text, @Nullable String subtext)
	{
		this(text, subtext, 0);
	}

	public RawSuggestion(String text, @Nullable String subtext, int priority)
	{
		super(subtext, priority);
		this.text = text;
	}

	@Override public String getText()
	{
		return text;
	}

	@Override public boolean match(String str)
	{
		return text.equals(str);
	}

	@Override public boolean matchPrefix(String prefix)
	{
		return matchPrefix(text, prefix);
	}
}
