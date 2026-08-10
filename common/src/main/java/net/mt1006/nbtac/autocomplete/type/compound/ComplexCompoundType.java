package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

public abstract class ComplexCompoundType extends AbstractCompoundType
{
	@Override protected @Nullable NbtTagMap getTagMap(SuggestionListContext ctx, ParsedCompound parsed)
	{
		NbtTagMap tagMap = new NbtTagMap();
		getBasicCompoundSuggestions(ctx, parsed, tagMap);
		return tagMap;
	}

	protected abstract void getBasicCompoundSuggestions(SuggestionListContext ctx, ParsedCompound parsed, NbtTagMap map);
}
