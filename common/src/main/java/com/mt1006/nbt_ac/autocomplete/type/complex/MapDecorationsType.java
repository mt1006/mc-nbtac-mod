package com.mt1006.nbt_ac.autocomplete.type.complex;

import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.TagSuggestion;
import com.mt1006.nbt_ac.autocomplete.type.PrimitiveType;
import com.mt1006.nbt_ac.autocomplete.type.Type;

import java.util.Random;

public class MapDecorationsType extends ComplexType
{
	public static final MapDecorationsType INSTANCE = new MapDecorationsType();
	private static final Random RNG = new Random();

	private MapDecorationsType()
	{
		super(PrimitiveType.COMPOUND);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: todo
	}

	@Override public void getCompoundSuggestions(SuggestionListContext ctx)
	{
		//TODO: text decorationId with digit as first char

		// 36^6 > 2^31
		String newDecorationId = Long.toString(Math.abs(RNG.nextInt()), Math.min(Character.MAX_RADIX, 36));
		Type decorationType = new TagsType("compound/nbtac:map_decoration", false);

		ctx.list().add(new TagSuggestion(new NbtSuggestion(newDecorationId, decorationType), ctx.parserType()));

		//TODO: test if it's necessary
		if (ctx.parentInfo().tagMap == null) { return; }
		for (String tag : ctx.parentInfo().tagMap.keySet())
		{
			ctx.list().add(new TagSuggestion(new NbtSuggestion(tag, decorationType), ctx.parserType()));
		}
	}
}
