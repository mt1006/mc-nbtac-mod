package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnumType extends ComplexType
{
	private final List<String> elements;
	private final boolean ordered;

	public EnumType(@Nullable Type type, List<String> args, boolean ordered)
	{
		super(type != null ? type.getPrimitive() : PrimitiveType.STRING);
		this.elements = args;
		this.ordered = ordered;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		int priority = 99; // start with 99, because order is from first to last, and priority < 0 means irrelevant
		for (String element : elements)
		{
			ctx.list().add(CustomSuggestion.fromType(element, null, this, ctx.parserType(), priority));
			if (!ordered) { priority--; }
		}
	}
}
