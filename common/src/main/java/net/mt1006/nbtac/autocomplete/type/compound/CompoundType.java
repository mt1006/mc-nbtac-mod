package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

public class CompoundType extends AbstractCompoundType
{
	private @Nullable NbtTagMap tagMap = null;

	public static CompoundType fromName(@Nullable String name)
	{
		if (name == null) { return new CompoundType(null); }

		NbtTagMap tagMap = NbtTagManager.get(name);
		return (name.startsWith("entity/") || name.startsWith("block/"))
				? new EntityCompoundType(tagMap, name.substring(name.indexOf('/') + 1))
				: new CompoundType(tagMap);
	}

	public CompoundType() {}

	protected CompoundType(@Nullable NbtTagMap tagMap)
	{
		this.tagMap = tagMap;
	}

	@Override public NbtTagMap getMutableTagMap()
	{
		if (tagMap == null) { tagMap = new NbtTagMap(); }
		return tagMap;
	}

	@Override public void setTagMap(@Nullable NbtTagMap subcompound)
	{
		tagMap = subcompound;
	}

	@Override public @Nullable NbtTagMap getSuggestionsTagMap(ParsedCompound parsed)
	{
		return tagMap;
	}

	public boolean hasTagMap()
	{
		return tagMap != null;
	}
}
