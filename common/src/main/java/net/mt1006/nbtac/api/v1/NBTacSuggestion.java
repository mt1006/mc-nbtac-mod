package net.mt1006.nbtac.api.v1;

import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.RawSuggestion;
import org.apache.logging.log4j.util.InternalApi;
import org.jspecify.annotations.Nullable;

public interface NBTacSuggestion
{
	static NBTacSuggestion createRaw(String text, @Nullable String subtext, int priority)
	{
		return new RawSuggestion(text, subtext, priority);
	}

	String getText();

	@Nullable String getSubtext();

	boolean match(String str);

	boolean matchPrefix(String prefix);

	@InternalApi
	CustomSuggestion get();
}
