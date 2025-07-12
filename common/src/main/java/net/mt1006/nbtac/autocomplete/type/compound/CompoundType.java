package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

public class CompoundType extends AbstractCompoundType
{
	private @Nullable NbtTagMap tagMap = null;

	public CompoundType() {}

	public CompoundType(@Nullable NbtTagMap tagMap)
	{
		this.tagMap = tagMap;
	}

	@Override public NbtTagMap getSubcompound()
	{
		if (tagMap == null) { tagMap = new NbtTagMap(); }
		return tagMap;
	}

	@Override public void setSubcompound(@Nullable NbtTagMap subcompound)
	{
		tagMap = subcompound;
	}

	@Override protected @Nullable NbtTagMap getTagMap(SuggestionListContext ctx, ParsedCompound parsed)
	{
		return tagMap;
	}
}
