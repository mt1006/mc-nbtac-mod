package net.mt1006.nbtac.autocomplete.suggestions;

import com.mojang.brigadier.StringReader;
import net.mt1006.nbtac.autocomplete.parser.ParserType;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StringSuggestion extends CustomSuggestion
{
	private final String visibleText;
	protected final List<String> matching = new ArrayList<>();
	protected final boolean withHiddenNamespace;

	public StringSuggestion(String text, @Nullable String subtext, ParserType parserType)
	{
		this(text, subtext, parserType, StringType.OTHER, 0);
	}

	public StringSuggestion(String text, @Nullable String subtext, ParserType parserType, int priority)
	{
		this(text, subtext, parserType, StringType.OTHER, priority);
	}

	protected StringSuggestion(String text, @Nullable String subtext, ParserType parserType, StringType stringType, int priority)
	{
		super(subtext, priority);

		withHiddenNamespace = ((stringType == StringType.ID && ModConfig.hideMcNamespaceInStrings.val)
				|| (stringType == StringType.ID_TAG && ModConfig.hideMcNamespaceInTags.val))
				&& text.startsWith("minecraft:") && !parserType.requiresNamespace;
		String rawText = getRawText(text, parserType.requiresDoubleQuotes);
		this.visibleText = getVisibleText(rawText, stringType, parserType.requiresDoubleQuotes);
		matching.add(rawText);
	}

	@Override public String getText()
	{
		return visibleText;
	}

	@Override public boolean match(String str)
	{
		if (str.startsWith("\"") || str.startsWith("'"))
		{
			if (str.charAt(str.length() - 1) != str.charAt(0) || str.length() < 2) { return false; }
			str = str.substring(1, str.length() - 1);
		}

		for (String id : matching)
		{
			if (id.equals(str)) { return true; }
		}
		return false;
	}

	@Override public boolean matchPrefix(String prefix)
	{
		if (prefix.startsWith("\"") || prefix.startsWith("'")) { prefix = prefix.substring(1); }

		for (String id : matching)
		{
			if (matchPrefix(id, prefix)) { return true; }
		}
		return false;
	}

	private String getRawText(String text, boolean isRawJson)
	{
		if (withHiddenNamespace) { text = text.substring(10); }

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

	private static String getVisibleText(String text, StringType stringType, boolean isRawJson)
	{
		// it's mostly redoing job of getRawText() because Java 21 and older require super() to be the first statement in the constructor
		//TODO: optimize or merge in some very distant future when updating to Java newer than 21
		boolean requiresQuotes = ((stringType == StringType.TAG)
					? ModConfig.tagQuotationMarks.val
					: ModConfig.stringQuotationMarks.val)
				|| isRawJson || strRequiresQuotes(text);

		if (!requiresQuotes)
		{
			return text;
		}
		else
		{
			char quoteChar = ModConfig.getDefaultQuotationMark(isRawJson);
			return quoteChar + text + quoteChar;
		}
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
		// in other case it would be treated as boolean
		if (str.equals("true") || str.equals("false")) { return true; }

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
		ID_TAG,
		ID,
		FULL_ID,
		OTHER
	}
}
