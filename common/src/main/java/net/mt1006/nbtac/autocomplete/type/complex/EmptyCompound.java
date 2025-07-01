package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

public class EmptyCompound extends ComplexType
{
	public static final EmptyCompound INSTANCE = new EmptyCompound();

	private EmptyCompound()
	{
		super(PrimitiveType.COMPOUND);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		ctx.list().addRaw("{}", "[#empty_compound]");
	}
}
