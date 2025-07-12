package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.network.chat.TextColor;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.StringSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.TextColorFields;

import java.util.Map;

public class TextColorType extends ComplexType
{
	public static final TextColorType INSTANCE = new TextColorType();

	private TextColorType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		Map<String, TextColor> colorMap = TextColorFields.getNAMED_COLORS();
		if (colorMap == null) { return; }

		for (Map.Entry<String, TextColor> entry : colorMap.entrySet())
		{
			String subtext = String.format("(#%06X) [#text_color]", entry.getValue().getValue());
			list.add(new StringSuggestion(entry.getKey(), subtext, ctx.parserType()));
		}
	}
}
