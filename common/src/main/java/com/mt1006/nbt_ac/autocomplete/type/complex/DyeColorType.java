package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.StringSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import net.minecraft.world.item.DyeColor;

public class DyeColorType extends ComplexType
{
	public static final DyeColorType INSTANCE = new DyeColorType();

	private DyeColorType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		for (DyeColor color : DyeColor.values())
		{
			ctx.list().add(new StringSuggestion(color.getName(), "[#dye_color]", ctx.parserType()));
		}
	}
}
