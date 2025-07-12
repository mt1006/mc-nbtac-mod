package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

public class ParsedTag
{
	public final ParsedCompound parentCompound;
	public @Nullable String key;
	public @Nullable ParsedValue val;

	public ParsedTag(ParsedCompound parent)
	{
		this.parentCompound = parent;
	}
}
