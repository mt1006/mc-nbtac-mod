package net.mt1006.nbtac.autocomplete.suggestions;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import org.jetbrains.annotations.Nullable;

public class IdSuggestion extends StringSuggestion
{
	public IdSuggestion(@Nullable Identifier id, @Nullable String subtext, ParserType parserType)
	{
		this(id, subtext, parserType, 0, false);
	}

	public IdSuggestion(@Nullable Identifier id, @Nullable String subtext, ParserType parserType, int priority, boolean isTagId)
	{
		super((isTagId ? "#" : "") + (id != null ? id.toString() : "error"), subtext,
				parserType, parserType.requiresNamespace ? StringType.FULL_ID : StringType.ID, priority);

		if (id != null)
		{
			matching.add(id.toString());
			if (isTagId) { matching.add("#" + id); }

			if (id.getNamespace().equals("minecraft"))
			{
				matching.add(id.getPath());
				if (isTagId) { matching.add("#" + id.getPath()); }
			}
		}
	}
}
