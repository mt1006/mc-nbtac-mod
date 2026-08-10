package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

public class ParsedList extends ParsedCollection<ParsedValue>
{
	public ParsedList(@Nullable ParsedTag parent, int pos)
	{
		super(parent, pos);
	}
}
