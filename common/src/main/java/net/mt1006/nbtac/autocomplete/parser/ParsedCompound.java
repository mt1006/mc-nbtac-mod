package net.mt1006.nbtac.autocomplete.parser;

import org.jetbrains.annotations.Nullable;

public class ParsedCompound extends ParsedCollection<ParsedTag>
{
	public ParsedCompound(@Nullable ParsedTag parent, int pos)
	{
		super(parent, pos);
	}

	public @Nullable ParsedTag get(String key)
	{
		for (ParsedTag tag : list)
		{
			if (key.equals(tag.key)) { return tag; }
		}
		return null;
	}

	public boolean containsKey(String key)
	{
		return get(key) != null;
	}

	public @Nullable String getStrVal(String key)
	{
		ParsedTag tag = get(key);
		return (tag != null && tag.val instanceof ParsedPrimitive primitive) ? primitive.val : null;
	}
}
