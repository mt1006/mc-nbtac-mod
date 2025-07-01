package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

import java.util.Random;

public class LongSeedType extends ComplexType
{
	public static final LongSeedType INSTANCE = new LongSeedType();
	private static final Random RNG = new Random();

	private LongSeedType()
	{
		super(PrimitiveType.LONG);
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		long random = RNG.nextLong();
		if (random == 0) { random = 123; } // there's about 1/10^19 it will happen, but it may happen

		ctx.list().addRaw("0", "(random) [#seed]", 1); // when it's set to 0, game will use random number
		ctx.list().addRaw(random + "l", "(constant) [#seed]");
	}
}
