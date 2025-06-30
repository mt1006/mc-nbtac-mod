package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.StringSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;

public class EntitySelectorType extends ComplexType
{
	public static final EntitySelectorType INSTANCE = new EntitySelectorType();

	private EntitySelectorType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: improve it using EntitySelectorParser
		ctx.list().add(new StringSuggestion("@p", "[#entity_selector]", ctx.parserType()));
		ctx.list().add(new StringSuggestion("@a", "[#entity_selector]", ctx.parserType()));
		ctx.list().add(new StringSuggestion("@r", "[#entity_selector]", ctx.parserType()));
		ctx.list().add(new StringSuggestion("@s", "[#entity_selector]", ctx.parserType()));
		ctx.list().add(new StringSuggestion("@e", "[#entity_selector]", ctx.parserType()));
		ctx.list().add(new StringSuggestion("@n", "[#entity_selector]", ctx.parserType()));
	}
}
