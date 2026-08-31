package net.mt1006.nbtac.autocomplete.type;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.type.complex.RegistryKeyType;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;

public class EitherType implements Type
{
	private final IdentityHashMap<Class<?>, Type> typeMap = new IdentityHashMap<>();

	public static EitherType registrySet(@Nullable String arg)
	{
		return new EitherType(List.of(
				new RegistryKeyType(arg, RegistryKeyType.Contents.BOTH),
				new ListType(new RegistryKeyType(arg))));
	}

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

	@Override public NbtTagMap getMutableTagMap()
	{
		Type type = typeMap.get(ParsedCompound.class);
		return type != null ? type.getMutableTagMap() : PrimitiveType.UNKNOWN.getMutableTagMap();
	}

	@Override public @Nullable NbtTagMap getSuggestionsTagMap(ParsedCompound parsed)
	{
		Type type = typeMap.get(ParsedCompound.class);
		return type != null ? type.getSuggestionsTagMap(parsed) : PrimitiveType.UNKNOWN.getSuggestionsTagMap(parsed);
	}

	@Override public void setTagMap(@Nullable NbtTagMap subcompound)
	{
		Type type = typeMap.get(ParsedCompound.class);
		if (type != null) { type.setTagMap(subcompound); }
		else { PrimitiveType.UNKNOWN.setTagMap(subcompound); }
	}
}
