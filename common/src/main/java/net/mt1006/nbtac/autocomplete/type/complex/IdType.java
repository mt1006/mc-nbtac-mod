package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

public class IdType extends ComplexType
{
	private final @Nullable ResourceLocation id;

	public IdType(@Nullable String id)
	{
		super(PrimitiveType.STRING);
		this.id = id != null ? ResourceLocation.tryParse(id) : null;
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		if (id != null) { list.add(new IdSuggestion(id, "[#id]", ctx.parserType())); }
	}
}
