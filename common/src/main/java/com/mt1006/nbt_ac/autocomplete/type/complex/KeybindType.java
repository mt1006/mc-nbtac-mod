package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.StringSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.mixin.fields.KeyMappingFields;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class KeybindType extends ComplexType
{
	//TODO: do something about suggestion list going out of the screen
	public static final KeybindType INSTANCE = new KeybindType();

	private KeybindType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		Map<String, KeyMapping> keyMap = KeyMappingFields.getALL();
		if (keyMap == null) { return; }

		for (String str : keyMap.keySet())
		{
			String subtext = "\"" + Component.translatable(str).getString() + "\" [#keybind]";
			ctx.list().add(new StringSuggestion(str, subtext, ctx.parserType()));
		}
	}
}
