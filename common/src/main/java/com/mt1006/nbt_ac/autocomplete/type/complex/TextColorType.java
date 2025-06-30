package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.StringSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.mixin.fields.TextColorFields;
import net.minecraft.network.chat.TextColor;

import java.util.Map;

public class TextColorType extends ComplexType
{
	public static final TextColorType INSTANCE = new TextColorType();

	private TextColorType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		Map<String, TextColor> colorMap = TextColorFields.getNAMED_COLORS();
		if (colorMap == null) { return; }

		for (Map.Entry<String, TextColor> entry : colorMap.entrySet())
		{
			String subtext = String.format("(#%06X) [#text_color]", entry.getValue().getValue());
			ctx.list().add(new StringSuggestion(entry.getKey(), subtext, ctx.parserType()));
		}
	}
}
