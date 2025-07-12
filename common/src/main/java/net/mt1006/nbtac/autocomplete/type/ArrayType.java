package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedArray;
import org.jetbrains.annotations.Nullable;

public class ArrayType implements Type
{
	public static final ArrayType BYTE = new ArrayType(PrimitiveType.BYTE_ARRAY, PrimitiveType.BYTE, 'B');
	public static final ArrayType INT = new ArrayType(PrimitiveType.INT_ARRAY, PrimitiveType.INT, 'I');
	public static final ArrayType LONG = new ArrayType(PrimitiveType.LONG_ARRAY, PrimitiveType.LONG, 'L');
	private final PrimitiveType primitive;
	private final PrimitiveType elementType;
	private final String opening;

	private ArrayType(PrimitiveType primitive, PrimitiveType elementType, char typeSymbol)
	{
		this.primitive = primitive;
		this.elementType = elementType;
		this.opening = "[" + typeSymbol + ";";
	}

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		if (!(ctx.parsed() instanceof ParsedArray parsed) || parsed.isEmpty())
		{
			// ctx.expectedOperators() could be null
			return new SuggestionList(ctx.parsed().pos).withOperators(opening);
		}
		if (parsed.isClosed()) { return null; }

		// e.g. [I;123
		SuggestionList list = elementType.getSuggestions(ctx.child(parsed.getLast()));
		return list != null ? list : new SuggestionList(ctx.reader().getCursor()).withOperators(",", "]");
	}

	@Override public PrimitiveType getPrimitive()
	{
		return primitive;
	}
}
