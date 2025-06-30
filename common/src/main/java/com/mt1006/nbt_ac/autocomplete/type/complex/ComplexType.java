package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.autocomplete.type.Type;

public abstract class ComplexType implements Type
{
	protected final PrimitiveType primitive;

	public ComplexType(PrimitiveType primitive)
	{
		this.primitive = primitive;
	}

	@Override public String getSubtext()
	{
		return getPrimitive().getSubtext();
	}

	@Override public PrimitiveType getPrimitive()
	{
		return primitive;
	}
}
