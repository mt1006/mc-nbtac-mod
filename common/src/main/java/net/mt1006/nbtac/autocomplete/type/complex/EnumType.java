package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;

import java.util.List;

public class EnumType extends ComplexType
{
	public static final EnumType DYE_COLOR = new EnumType(List.of(), List.of("white", "orange", "magenta", "light_blue",
			"yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"), false);
	private final List<String> elements;
	private final boolean ordered;

	public EnumType(List<Type> types, List<String> args, boolean ordered)
	{
		super(types.isEmpty() ? PrimitiveType.STRING : types.getFirst().getPrimitive());
		this.elements = args;
		this.ordered = ordered;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		int priority = 99; // start with 99, because order is from first to last, and priority < 0 means irrelevant
		for (String element : elements)
		{
			list.add(CustomSuggestion.fromType(element, null, this, ctx.parserType(), priority));
			if (!ordered) { priority--; }
		}
	}
}
