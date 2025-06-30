package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;

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
