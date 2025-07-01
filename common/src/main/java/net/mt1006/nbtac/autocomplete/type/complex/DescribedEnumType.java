package net.mt1006.nbtac.autocomplete.type.complex;

import com.mojang.datafixers.util.Pair;
import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DescribedEnumType extends ComplexType
{
	private final List<Pair<String, String>> elements = new ArrayList<>();

	public DescribedEnumType(@Nullable Type type, List<String> args)
	{
		super(type != null ? type.getPrimitive() : PrimitiveType.STRING);
		for (int i = 0; i < args.size() / 2; i++)
		{
			elements.add(Pair.of(args.get(i * 2), args.get((i * 2) + 1)));
		}
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		elements.forEach((e) -> ctx.list().add(CustomSuggestion.fromType(e.getFirst(),
				"<" + e.getSecond() + ">", this, ctx.parserType(), 0)));
	}
}
