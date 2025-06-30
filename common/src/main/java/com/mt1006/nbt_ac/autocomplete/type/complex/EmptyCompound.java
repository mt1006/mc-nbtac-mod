package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;

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
