package com.mt1006.nbt_ac.autocomplete.type;

import org.jetbrains.annotations.Nullable;

public class ListType implements Type
{
	public final Type elementType;

	public ListType(@Nullable Type elementType)
	{
		this.elementType = (elementType != null ? elementType : PrimitiveType.UNKNOWN);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		PrimitiveType.LIST.getSuggestions(ctx);
	}

	@Override public String getSubtext()
	{
		return PrimitiveType.LIST.getSubtext();
	}

	@Override public PrimitiveType getPrimitive()
	{
		return PrimitiveType.LIST;
	}
}
