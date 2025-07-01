package net.mt1006.nbtac.autocomplete.type.complex;

import net.mt1006.nbtac.autocomplete.NbtSuggestionManager;
import net.mt1006.nbtac.autocomplete.NbtSuggestionMap;
import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.TagSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import org.jetbrains.annotations.Nullable;

public class TagsType extends ComplexType
{
	private final @Nullable String id;
	private final boolean withId;

	public TagsType(@Nullable String id, boolean withId)
	{
		super(PrimitiveType.COMPOUND);
		if (id != null)
		{
			id = id.replace("block/item/", "block/");
			id = id.replace("entity/item/", "entity/");
		}
		this.id = id;
		this.withId = withId;
	}

	@Override public void getSuggestions(SuggestionListContext ctx)
	{
		//TODO: do something?
	}

	@Override public void getCompoundSuggestions(SuggestionListContext ctx)
	{
		if (id == null) { return; }
		NbtSuggestionMap tagSuggestions = NbtSuggestionManager.get(id);
		ctx.list().addAll(tagSuggestions, id, ctx.parserType());

		if (withId)
		{
			int dataSlashPos = id.indexOf('/');
			if (dataSlashPos == -1) { return; }

			NbtSuggestion tempSuggestion = new NbtSuggestion("id", new RequiredIdType(id.substring(dataSlashPos + 1)));
			ctx.list().add(new TagSuggestion(tempSuggestion, ctx.parserType(), 100));
		}
	}
}
