package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.mixin.fields.AtlasManagerFields;

import java.util.Collection;

public class AtlasType extends ComplexType
{
	public static final AtlasType INSTANCE = new AtlasType();

	private AtlasType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		Collection<Identifier> atlases = ((AtlasManagerFields)Minecraft.getInstance().getAtlasManager()).nbtac$getAtlasById().keySet();
		atlases.forEach((id) -> list.add(new IdSuggestion(id, "[#atlas]", ctx.parserType())));
	}
}
