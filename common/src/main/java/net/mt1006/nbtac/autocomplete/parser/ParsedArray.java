package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

public class ParsedArray extends ParsedCollection<ParsedValue>
{
	public ParsedArray(@Nullable ParsedTag parent, int pos)
	{
		super(parent, pos);
	}
}
