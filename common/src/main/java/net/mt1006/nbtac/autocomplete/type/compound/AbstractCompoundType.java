package net.mt1006.nbtac.autocomplete.type.compound;

import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedTag;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCompoundType implements Type
{
	@Override public @Nullable SuggestionList getSuggestions(SuggestionListContext ctx)
	{
		if (!(ctx.parsed() instanceof ParsedCompound parsed) || parsed.isEmpty())
		{
			// ctx.expectedOperators() could be null
			return new SuggestionList(ctx.parsed().pos).withOperators("{");
		}
		if (parsed.isClosed()) { return null; }

		ParsedTag lastTag = parsed.getLast();
		int lastTagPos = parsed.getLastPos();

		NbtTagMap tagMap = getSuggestionsTagMap(parsed);
		if (tagMap == null) { return SuggestionList.empty(); }

		if (ctx.expectedOperators() != null)
		{
			if (lastTag.key == null)
			{
				throw new RuntimeException("This should not happen!");
			}
			else if (lastTag.val == null) // e.g. {SomeKey
			{
				return tagMap.containsKey(lastTag.key)
						? ctx.expectedOperators()
						: tagMap.suggestionsForKeyPrefix(ctx.parserType(), parsed, lastTag.key, lastTagPos, true);
			}
		}
		else
		{
			if (lastTag.key == null) // e.g. { or {SomeKey:123,
			{
				return tagMap.suggestionsForKeyPrefix(ctx.parserType(), parsed, "", lastTagPos, true);
			}
		}

		// e.g. {SomeKey: or {SomeKey:123
		NbtTag tag = tagMap.get(lastTag.key);
		SuggestionList valList = tag != null ? tag.getType().getSuggestions(ctx.child(lastTag.val)) : SuggestionList.empty();
		return valList != null ? valList : new SuggestionList(ctx.reader().getCursor()).withOperators(",", "}");
	}

	@Override public PrimitiveType getPrimitive()
	{
		return PrimitiveType.COMPOUND;
	}
}
