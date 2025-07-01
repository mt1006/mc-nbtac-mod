package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.world.item.DyeColor;
import net.mt1006.nbtac.autocomplete.suggestions.StringSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

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
