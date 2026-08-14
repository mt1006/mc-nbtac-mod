package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;

public class EitherType implements Type
{
	private final IdentityHashMap<Class<?>, Type> typeMap = new IdentityHashMap<>();

	public EitherType(List<Type> types)
	{
		types.forEach((t) -> typeMap.put(t.getPrimitive().getClassOfParsed(), t));
	}

	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		Type type = typeMap.get(ctx.parsed().getClass());
		//TODO: show alternative types?
		/*if (type == null)
		{
			SuggestionList list = new SuggestionList();
			for (Type t : typeMap.values())
			{
				SuggestionList listToAppend = t.getSuggestions(ctx);
				if (listToAppend != null) { listToAppend.forEach(list::add); }
			}
			return list;
		}*/
		return type.getSuggestions(ctx);
	}

	@Override public String getSubtext()
	{
		return "[either]";
	}

	@Override public PrimitiveType getPrimitive()
	{
		return PrimitiveType.UNKNOWN;
	}

	@Override public NbtTagMap getSubcompound()
	{
		Type type = typeMap.get(ParsedCompound.class);
		return type != null ? type.getSubcompound() : PrimitiveType.UNKNOWN.getSubcompound();
	}
}
