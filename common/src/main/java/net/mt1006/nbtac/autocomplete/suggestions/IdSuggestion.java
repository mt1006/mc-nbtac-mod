package net.mt1006.nbtac.autocomplete.suggestions;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.CustomTagParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IdSuggestion extends StringSuggestion
{
	private final List<String> matchingIds = new ArrayList<>();

	public IdSuggestion(@Nullable ResourceLocation id, @Nullable String subtext,
						CustomTagParser.Type parserType, int priority, boolean isTagId)
	{
		super((isTagId ? "#" : "") + (id != null ? id.toString() : "_error"), subtext,
				parserType, parserType.requiresNamespace ? StringType.FULL_ID : StringType.ID, priority);

		if (id != null)
		{
			matchingIds.add(id.toString());
			if (isTagId) { matchingIds.add("#" + id); }

			if (id.getNamespace().equals("minecraft"))
			{
				matchingIds.add(id.getPath());
				if (isTagId) { matchingIds.add("#" + id.getPath()); }
			}
		}
	}

	public IdSuggestion(@Nullable ResourceLocation resLoc, @Nullable String subtext, CustomTagParser.Type parserType)
	{
		this(resLoc, subtext, parserType, 0, false);
	}

	@Override public boolean match(String str)
	{
		for (String id : matchingIds)
		{
			if (id.equals(str)) { return true; }
		}
		return false;
	}

	@Override public boolean matchUnfinished(String str)
	{
		for (String id : matchingIds)
		{
			if (matchPrefix(id, str)) { return true; }
		}
		return false;
	}
}
