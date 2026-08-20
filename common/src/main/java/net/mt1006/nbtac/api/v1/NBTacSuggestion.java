package net.mt1006.nbtac.api.v1;

import net.mt1006.nbtac.autocomplete.suggestions.CustomSuggestion;
import net.mt1006.nbtac.autocomplete.suggestions.RawSuggestion;
import org.apache.logging.log4j.util.InternalApi;
import org.jetbrains.annotations.Nullable;

public interface NBTacSuggestion
{
	/**
	 * Creates <b>raw</b> suggestion. Raw means it doesn't get special treatment like string or ID suggestions.
	 * It isn't automatically quoted depending on context, or isn't matched against partial strings.
	 * @param text suggestion string
	 * @param subtext suggestion subtext - grayed out text
	 * @param priority - suggestion priority, see getPriority() for more details
	 * @return suggestion object
	 */
	static NBTacSuggestion createRaw(String text, @Nullable String subtext, int priority)
	{
		return new RawSuggestion(text, subtext, priority);
	}

	/**
	 * @return suggestion string
	 */
	String getText();

	/**
	 * @return suggestion subtext - grayed out text next to main suggestion
	 */
	@Nullable String getSubtext();

	/**
	 * Returns priority of a suggestion. Priority determines position in a suggestion list.
	 * The higher priority, the higher position, unless this behavior is disabled in settings.
	 * It also determines if suggestion is recommended or irrelevant:
	 * < 0 - irrelevant suggestion
	 * 0 - 99 - normal suggestion
	 * >= 100 - recommended suggestion
	 * How these are interpreted also depends on settings.
	 * @return priority of a suggestion
	 */
	int getPriority();

	/**
	 * Checks if given str matches suggestion.
	 * This isn't same as getText().equals(), as for example ID suggestion "\"minecraft:cow\"" might match "cow".
	 * Return value might depend on user settings.
	 * @param str string to match
	 * @return true if matched, false otherwise
	 */
	boolean match(String str);

	/**
	 * Checks if given string prefix matches suggestion.
	 * Just like match(), it's not simple prefix check against getText(), for ID suggestion "\"minecraft:cow\"",
	 * this function with prefix "co" might return true.
	 * Return value might depend on user settings.
	 * @param prefix prefix to match
	 * @return true if prefix matched, false otherwise
	 */
	boolean matchPrefix(String prefix);

	@InternalApi
	CustomSuggestion get();
}
