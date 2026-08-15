package net.mt1006.nbtac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Fields;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public abstract class CustomSuggestion
{
	private final @Nullable String subtext;
	private final int priority;

	public CustomSuggestion(@Nullable String subtext, int priority)
	{
		this.subtext = getMarkedSubtext(subtext, priority);
		this.priority = priority;
	}

	public static CustomSuggestion fromType(String text, @Nullable String subtext, Type type,
											ParserType parserType, int priority)
	{
		return type.getPrimitive() == PrimitiveType.STRING
				? new StringSuggestion(text, subtext, parserType, priority)
				: new RawSuggestion(text, subtext, priority);
	}

	public abstract String getText();

	public @Nullable Message getTooltip()
	{
		return null;
	}

	public abstract boolean match(String str);

	public abstract boolean matchPrefix(String prefix);

	protected static boolean matchPrefix(String str, String prefix)
	{
		return ModConfig.ignoreLetterCase.val
				? str.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))
				: str.startsWith(prefix);
	}

	public void suggest(SuggestionsBuilder builder)
	{
		String visibleText = getText();
		Message tooltip = getTooltip();
		boolean isEmptySuggestion;

		if (visibleText.isEmpty())
		{
			if (!addEmptySuggestion(builder, tooltip))
			{
				NBTac.LOGGER.error("Something went wrong while adding empty suggestion!");
				return;
			}
			isEmptySuggestion = true;
		}
		else
		{
			builder.suggest(visibleText, tooltip);
			isEmptySuggestion = false;
		}

		if (!ModConfig.showTagHints.val) { return; }

		SuggestionManager.clearIfNeeded(builder);

		Suggestion lastAdded = getLastAddedSuggestion(builder);
		if (lastAdded != null) { SuggestionManager.dataMap.put(lastAdded, new Data(subtext, priority, isEmptySuggestion)); }
		SuggestionManager.hasCustomSuggestions = true;
	}

	private static boolean addEmptySuggestion(SuggestionsBuilder builder, @Nullable Message tooltip)
	{
		if (!ModConfig.showTagHints.val) { return true; }
		if (Fields.suggestionsBuilderList == null) { return false; }

		try
		{
			List<Suggestion> suggestions = (List<Suggestion>)Fields.suggestionsBuilderList.get(builder);
			int start = (int)Fields.suggestionsBuilderInt.get(builder);

			int len = 0;
			for (Field stringField : Fields.suggestionsBuilderStrings)
			{
				String val = (String)stringField.get(builder);
				if (val != null && val.length() > len) { len = val.length(); }
			}

			suggestions.add(new Suggestion(StringRange.between(start, len), "", tooltip));
			return true;
		}
		catch (Exception e) { return false; }
	}

	private static @Nullable Suggestion getLastAddedSuggestion(SuggestionsBuilder builder)
	{
		if (Fields.suggestionsBuilderList == null) { return null; }

		try
		{
			List<Suggestion> suggestionList = (List<Suggestion>)Fields.suggestionsBuilderList.get(builder);
			return suggestionList.get(suggestionList.size() - 1);
		}
		catch (Exception e) { return null; }
	}

	private static @Nullable String getMarkedSubtext(@Nullable String subtext, int priority)
	{
		if (subtext != null && !subtext.isEmpty())
		{
			if (priority >= 100 && ModConfig.markRecommended.val) { return "<*> " + subtext; }
			if (priority < 0 && ModConfig.markIrrelevant.val) { return "<I> " + subtext; }
		}
		else
		{
			if (priority >= 100 && ModConfig.markRecommended.val) { return "<*>"; }
			if (priority < 0 && ModConfig.markIrrelevant.val) { return "<I>"; }
		}

		return subtext;
	}

	public static class Data
	{
		private static final Data ERROR = new Data("error, pls report", 9999, false);
		public final String subtext;
		public final int priority;
		public int order = 0;

		public Data(@Nullable String subtext, int priority, boolean isEmptySuggestion)
		{
			this.subtext = (subtext == null || isEmptySuggestion || subtext.isEmpty() || subtext.charAt(0) == ' ')
					? subtext
					: ("  " + subtext);
			this.priority = priority;
		}

		public static Data error()
		{
			ERROR.order = ERROR.priority;
			return ERROR;
		}
	}
}
