package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.Fields;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public abstract class CustomSuggestion
{
	protected final String text;
	private final @Nullable String subtext;
	private final int priority;

	protected CustomSuggestion(String text, @Nullable String subtext, int priority)
	{
		this.text = text;
		this.subtext = getMarkedSubtext(subtext, priority);
		this.priority = priority;
	}

	public static CustomSuggestion fromType(String text, @Nullable String subtext, NbtSuggestion.Type type,
											CustomTagParser.Type parserType, int priority)
	{
		return type == NbtSuggestion.Type.STRING
				? new StringSuggestion(text, subtext, parserType, priority)
				: new RawSuggestion(text, subtext, priority);
	}

	public String getVisibleText()
	{
		return text;
	}

	public @Nullable Message getTooltip()
	{
		return null;
	}

	public boolean match(String str)
	{
		return text.equals(str);
	}

	public boolean matchUnfinished(String str)
	{
		return matchPrefix(text, str);
	}

	protected static boolean matchPrefix(String str, String prefix)
	{
		if (ModConfig.ignoreLetterCase.val) { return str.toLowerCase().startsWith(prefix.toLowerCase()); }
		else { return str.startsWith(prefix); }
	}

	public void suggest(SuggestionsBuilder suggestionsBuilder)
	{
		String visibleText = getVisibleText();
		Message tooltip = getTooltip();
		boolean isEmptySuggestion;

		if (visibleText.isEmpty() && suggestionsBuilder.getRemaining().isEmpty())
		{
			if (!addEmptySuggestion(suggestionsBuilder, tooltip))
			{
				NBTac.LOGGER.error("Something went wrong while adding empty suggestion!");
				return;
			}
			isEmptySuggestion = true;
		}
		else
		{
			suggestionsBuilder.suggest(visibleText, tooltip);
			isEmptySuggestion = false;
		}

		if (!ModConfig.showTagHints.val) { return; }

		Suggestion lastAdded = getLastAddedSuggestion(suggestionsBuilder);
		if (lastAdded != null) { NbtSuggestionManager.dataMap.put(lastAdded, new Data(subtext, priority, isEmptySuggestion)); }
		NbtSuggestionManager.hasCustomSuggestions = true;
	}

	private static boolean addEmptySuggestion(SuggestionsBuilder suggestionsBuilder, @Nullable Message tooltip)
	{
		if (!ModConfig.showTagHints.val) { return true; }
		if (Fields.suggestionsBuilderList == null) { return false; }

		try
		{
			List<Suggestion> suggestions = (List<Suggestion>)Fields.suggestionsBuilderList.get(suggestionsBuilder);
			int start = (int)Fields.suggestionsBuilderInt.get(suggestionsBuilder);

			int len = 0;
			for (Field stringField : Fields.suggestionsBuilderStrings)
			{
				String val = (String)stringField.get(suggestionsBuilder);
				if (val != null && val.length() > len) { len = val.length(); }
			}

			suggestions.add(new Suggestion(StringRange.between(start, len), "", tooltip));
			return true;
		}
		catch (Exception exception) { return false; }
	}

	private static @Nullable Suggestion getLastAddedSuggestion(SuggestionsBuilder suggestionsBuilder)
	{
		if (Fields.suggestionsBuilderList == null) { return null; }

		try
		{
			List<Suggestion> suggestionList = (List<Suggestion>)Fields.suggestionsBuilderList.get(suggestionsBuilder);
			return suggestionList.get(suggestionList.size() - 1);
		}
		catch (Exception exception) { return null; }
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
