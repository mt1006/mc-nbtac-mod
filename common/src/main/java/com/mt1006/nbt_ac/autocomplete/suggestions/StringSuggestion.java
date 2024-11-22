package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.StringReader;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class StringSuggestion extends CustomSuggestion
{
	private final String quotedText;

	protected StringSuggestion(String text, @Nullable String subtext, CustomTagParser.Type parserType, StringType stringType, int priority)
	{
		super(getFinalText(text, stringType, parserType.requiresDoubleQuotes), subtext, priority);
		this.quotedText = getQuotedText(this.text, stringType, parserType.requiresDoubleQuotes);
	}

	public StringSuggestion(String text, @Nullable String subtext, CustomTagParser.Type parserType, int priority)
	{
		this(text, subtext, parserType, StringType.OTHER, priority);
	}

	public StringSuggestion(String text, @Nullable String subtext, CustomTagParser.Type parserType)
	{
		this(text, subtext, parserType, StringType.OTHER, 0);
	}

	@Override public String getVisibleText()
	{
		return quotedText;
	}

	private static String getFinalText(String text, StringType stringType, boolean isRawJson)
	{
		if (((stringType == StringType.ID && ModConfig.hideMcNamespaceInStrings.val)
				|| (stringType == StringType.TAG_ID && ModConfig.hideMcNamespaceInTags.val))
				&& text.startsWith("minecraft:"))
		{
			text = text.substring(10);
		}

		char quoteChar = ModConfig.getDefaultQuotationMark(isRawJson);
		if (!strRequiresParsing(text, quoteChar)) { return text; }

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < text.length(); i++)
		{
			char ch = text.charAt(i);
			if (ch == '\\' || ch == quoteChar) { builder.append('\\'); }
			builder.append(ch);
		}

		return builder.toString();
	}

	private static String getQuotedText(String newText, StringType stringType, boolean isRawJson)
	{
		// it's mostly redoing job of getFinalText() because Java 21 and older require super() to be the first statement in the constructor
		//TODO: optimize or merge in some very distant future when updating to Java newer than 21
		boolean requiresQuotes = ((stringType == StringType.TAG) ? ModConfig.tagQuotationMarks.val : ModConfig.stringQuotationMarks.val) || isRawJson;

		if (!requiresQuotes)
		{
			requiresQuotes = strRequiresQuotes(newText);
			if (!requiresQuotes) { return newText; }
		}

		char quoteChar = ModConfig.getDefaultQuotationMark(isRawJson);
		return String.format("%c%s%c", quoteChar, newText, quoteChar);
	}

	protected static boolean strRequiresParsing(String str, char quoteChar)
	{
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (ch == quoteChar || ch == '\\') { return true; }
		}
		return false;
	}

	protected static boolean strRequiresQuotes(String str)
	{
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);

			// dot requires quotes to prevent issues with tag suggestions in NBT paths
			if (!StringReader.isAllowedInUnquotedString(ch) || ch == '.') { return true; }
		}
		return false;
	}

	protected enum StringType
	{
		TAG,
		TAG_ID,
		ID,
		FULL_ID,
		OTHER
	}
}
