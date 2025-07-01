package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.mt1006.nbtac.autocomplete.suggestions.IdSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.utils.Fields;

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
