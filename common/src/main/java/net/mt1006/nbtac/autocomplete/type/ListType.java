package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedList;
import org.jetbrains.annotations.Nullable;

public class ListType implements Type
{
	private final Type elementType;

	public ListType(Type elementType)
	{
		this.elementType = (elementType != null ? elementType : PrimitiveType.UNKNOWN);
	}

	@Override public SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		if (!(ctx.parsed() instanceof ParsedList parsed) || parsed.isEmpty())
		{
			// ctx.expectedOperators() could be null
			return new SuggestionList(ctx.parsed().pos).withOperators("[");
		}
		if (parsed.isClosed()) { return null; }

		// e.g. [123
		SuggestionList list = elementType.getSuggestions(ctx.child(parsed.getLast()));
		return list != null ? list : new SuggestionList(ctx.reader().getCursor()).withOperators(",", "]");
	}

	@Override public PrimitiveType getPrimitive()
	{
		return PrimitiveType.LIST;
	}

	@Override public NbtTagMap getSubcompound()
	{
		return elementType.getSubcompound();
	}

	@Override public void setSubcompound(@Nullable NbtTagMap subcompound)
	{
		elementType.setSubcompound(subcompound);
	}

	public Type getElementType()
	{
		return elementType;
	}
}
