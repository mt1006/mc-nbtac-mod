package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.StringSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

import java.util.Locale;

public class MapDecorationTypeType extends ComplexType
{
	public static final MapDecorationTypeType INSTANCE = new MapDecorationTypeType();

	private MapDecorationTypeType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		for (MapDecoration.Type type : MapDecoration.Type.values())
		{
			list.add(new StringSuggestion(type.name().toLowerCase(Locale.ROOT), "[#map_decoration_type]", ctx.parserType()));
		}
	}
}
