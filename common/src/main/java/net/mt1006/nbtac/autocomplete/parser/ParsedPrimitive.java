package net.mt1006.nbtac.autocomplete.parser;

import net.mt1006.nbtac.utils.SimpleStringReader;
import org.jetbrains.annotations.Nullable;

public class ParsedPrimitive extends ParsedValue
{
	public @Nullable String val;
	public boolean closed = false;

	public ParsedPrimitive(@Nullable ParsedTag parent, int pos)
	{
		super(parent, pos);
	}

	public void setFromReader(SimpleStringReader.StringResults results)
	{
		this.val = results.str;
		this.closed = results.closed;
	}
}
