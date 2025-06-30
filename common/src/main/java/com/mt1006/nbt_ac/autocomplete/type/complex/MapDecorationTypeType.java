package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.IdSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.utils.Fields;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

import java.util.List;

public class MapDecorationTypeType extends ComplexType
{
	public static final MapDecorationTypeType INSTANCE = new MapDecorationTypeType();

	private MapDecorationTypeType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: check why it even works?
		List<Holder> decorationTypes = Fields.getStaticFields(MapDecorationTypes.class, Holder.class);
		for (Holder<ResourceKey> holder : decorationTypes)
		{
			ResourceKey<?> key = holder.unwrapKey().orElse(null);
			if (key != null) { ctx.list().add(new IdSuggestion(key.location(), "[#map_decoration_type]", ctx.parserType())); }
		}
	}
}
