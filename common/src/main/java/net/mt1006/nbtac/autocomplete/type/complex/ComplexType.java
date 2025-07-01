package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;

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
