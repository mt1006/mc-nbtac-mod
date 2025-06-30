package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.config.ModConfig;

public class TextComponentType extends ComplexType
{
	public static final TextComponentType INSTANCE = new TextComponentType();

	private TextComponentType()
	{
		super(PrimitiveType.COMPOUND);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		ctx.list().addRaw(ModConfig.getDefaultQuotationMarkStr(false), "(simple string) [#text_component]", 3);
		ctx.list().addRaw("{", "(text component) [#text_component]", 2);
		ctx.list().addRaw("[", "(test component list) [#text_component]", 1);
	}
}
