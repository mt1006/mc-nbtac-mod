package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

public abstract class ParsedValue
{
	public final @Nullable ParsedTag parentTag;
	public final int pos;

	public ParsedValue(@Nullable ParsedTag parent, int pos)
	{
		this.parentTag = parent;
		this.pos = pos;
	}
}
