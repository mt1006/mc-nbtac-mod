package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
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
