package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

public class ArmorStandSlotsType extends ComplexType
{
	public static ArmorStandSlotsType INSTANCE = new ArmorStandSlotsType();

	private ArmorStandSlotsType()
	{
		super(PrimitiveType.INT);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		//TODO: finish
	}
}
