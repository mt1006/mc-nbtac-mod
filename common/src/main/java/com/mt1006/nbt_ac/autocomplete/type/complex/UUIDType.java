package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;

import java.util.UUID;

public class UUIDType extends ComplexType
{
	public static final UUIDType RANDOM = new UUIDType(true);
	public static final UUIDType UNKNOWN = new UUIDType(false);
	private final boolean random;

	private UUIDType(boolean random)
	{
		super(PrimitiveType.INT_ARRAY);
		this.random = random;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		if (random)
		{
			UUID randomUUID = UUID.randomUUID();
			int uuidInt0 = (int)randomUUID.getLeastSignificantBits();
			int uuidInt1 = (int)(randomUUID.getLeastSignificantBits() >>> 32);
			int uuidInt2 = (int)randomUUID.getMostSignificantBits();
			int uuidInt3 = (int)(randomUUID.getMostSignificantBits() >>> 32);

			//TODO: add setting to remove spaces?
			String uuidString = String.format("[I;%d, %d, %d, %d]", uuidInt3, uuidInt2, uuidInt1, uuidInt0);
			ctx.list().addRaw(uuidString, "[#random_uuid]");
		}
		else
		{
			PrimitiveType.INT_ARRAY.getSuggestions(ctx);
		}
	}
}
