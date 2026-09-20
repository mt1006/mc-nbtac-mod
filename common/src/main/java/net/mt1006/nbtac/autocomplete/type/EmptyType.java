package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import org.jetbrains.annotations.Nullable;

public class EmptyType implements Type
{
	public static final EmptyType INSTANCE = new EmptyType();

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		return SuggestionList.empty();
	}

	@Override public PrimitiveType getPrimitive()
	{
		return PrimitiveType.UNKNOWN;
	}
}
