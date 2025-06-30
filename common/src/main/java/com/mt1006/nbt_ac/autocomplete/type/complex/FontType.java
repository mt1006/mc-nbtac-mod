package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.mixin.fields.FontManagerFields;
import com.mt1006.nbt_ac.mixin.fields.MinecraftFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.resources.ResourceLocation;

public class FontType extends ComplexType
{
	public static final FontType INSTANCE = new FontType();

	private FontType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		FontManager fontManager = ((MinecraftFields) Minecraft.getInstance()).getFontManager();
		for (ResourceLocation id : ((FontManagerFields)fontManager).getFontSets().keySet())
		{
			ctx.list().add(new IdSuggestion(id, "[#font]", ctx.parserType()));
		}
	}
}
