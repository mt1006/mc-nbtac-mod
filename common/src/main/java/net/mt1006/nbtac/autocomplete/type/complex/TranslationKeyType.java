package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

public class TranslationKeyType extends ComplexType
{
	public static final TranslationKeyType INSTANCE = new TranslationKeyType();

	public TranslationKeyType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: implement
	}
}
