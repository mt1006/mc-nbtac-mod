package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

public class RequiredIdType extends ComplexType
{
	private final @Nullable ResourceLocation id;

	public RequiredIdType(@Nullable String id)
	{
		super(PrimitiveType.STRING);
		this.id = id != null ? ResourceLocation.tryParse(id) : null;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		if (id != null) { ctx.list().add(new IdSuggestion(id, "[#required_id]", ctx.parserType())); }
	}
}
