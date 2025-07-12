package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedTag;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.Type;

import java.util.Random;

public class MapDecorationsType extends ComplexCompoundType
{
	public static final MapDecorationsType INSTANCE = new MapDecorationsType();
	private static final Random RNG = new Random();

	@Override public void getBasicCompoundSuggestions(SuggestionListContext ctx, ParsedCompound parsed, NbtTagMap map)
	{
		// 36^6 > 2^31
		String newDecorationId = Long.toString(Math.abs(RNG.nextInt()), Math.min(Character.MAX_RADIX, 36));
		Type tagType = new CompoundType(NbtTagManager.get("compound/nbtac:map_decoration"));

		// There's issue with potential tag duplications but considering there are 2^31 variants
		// I don't think it's worth preventing.
		// It should be done this way (with random names) instead of for example tag1, tag2, tag3...
		// as changing decoration data without changing tag name won't change it.
		map.add(new GeneratedNbtTag(newDecorationId, tagType).withSubtext("[#random_tag]"));

		for (ParsedTag tag : parsed.getAll())
		{
			if (tag.key != null) { map.add(new GeneratedNbtTag(tag.key, tagType).withSubtext("[#random_tag]")); }
		}
	}
}
