package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import net.minecraft.resources.ResourceLocation;
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
