package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.FontManagerFields;
import net.mt1006.nbtac.mixin.fields.MinecraftFields;

public class FontType extends ComplexType
{
	public static final FontType INSTANCE = new FontType();

	private FontType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		FontManager fontManager = ((MinecraftFields)Minecraft.getInstance()).getFontManager();
		for (Identifier id : ((FontManagerFields)fontManager).getFontSets().keySet())
		{
			list.add(new IdSuggestion(id, "[#font]", ctx.parserType()));
		}
	}
}
