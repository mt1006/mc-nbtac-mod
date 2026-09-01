package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

public abstract class ComplexCompoundType extends AbstractCompoundType
{
	@Override public @Nullable NbtTagMap getSuggestionsTagMap(ParsedCompound parsed)
	{
		NbtTagMap tagMap = new NbtTagMap();
		getBasicCompoundSuggestions(parsed, tagMap);
		return tagMap;
	}

	protected abstract void getBasicCompoundSuggestions(ParsedCompound parsed, NbtTagMap map);
}
