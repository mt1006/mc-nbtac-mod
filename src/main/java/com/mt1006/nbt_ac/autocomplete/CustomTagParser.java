package com.mt1006.nbt_ac.autocomplete;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestionSubtype;
import com.mt1006.nbt_ac.autocomplete.suggestions.TagSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.nbt.TagParser;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomTagParser
{
	private static final NbtSuggestion JSON_DUMMY_NBT_SUGGESTION = new NbtSuggestion("nbt_ac:json_dummy",
			NbtSuggestion.Type.LIST, NbtSuggestion.Source.DEFAULT, NbtSuggestion.Type.COMPOUND);

	private final StringReader reader;
	private final Type parserType;
	public @Nullable NbtSuggestion lastFoundSuggestion = null;

	public CustomTagParser(String tag, Type parserType)
	{
		this.reader = new StringReader(tag);
		this.parserType = parserType;
	}

	public static Pair<Suggestion, Integer> parseJsonComponent(SuggestionList suggestionList, String str, boolean inner)
	{
		if (!inner && !Loader.finished) { return Pair.of(Suggestion.NONE, 0); } //TODO: add "not loaded" message

		boolean isList = str.startsWith("[");
		JSON_DUMMY_NBT_SUGGESTION.subtype = NbtSuggestionSubtype.JSON_TEXT_COMPOUND;

		CustomTagParser jsonParser = new CustomTagParser(str, isList ? Type.JSON_LIST : Type.JSON);
		SuggestionList jsonSuggestions = new SuggestionList();
		Suggestion jsonSuggestion = jsonParser.read(jsonSuggestions, JSON_DUMMY_NBT_SUGGESTION, null);

		if (jsonSuggestion == Suggestion.TAG) { suggestionList.replaceWith(jsonSuggestions); }
		return Pair.of(jsonSuggestion.asJsonSuggestion(inner), jsonParser.getCursor());
	}

	public SuggestionList prepareSuggestionList(@Nullable NbtSuggestions suggestions, @Nullable String rootTag)
	{
		SuggestionList suggestionList = new SuggestionList();
		suggestionList.addAll(suggestions, rootTag, parserType);
		return suggestionList;
	}

	public Suggestion read(SuggestionList suggestionList, @Nullable NbtSuggestion suggestion, @Nullable String parentTag)
	{
		try
		{
			return switch (parserType)
			{
				case COMPOUND -> readStruct(suggestionList, suggestion, NbtSuggestion.ParentInfo.fromRoot(parentTag), true, false);
				case PATH -> readPath(suggestionList);
				case JSON -> readStruct(suggestionList, suggestion, NbtSuggestion.ParentInfo.blank(), true, false);
				case JSON_LIST -> readList(suggestionList, suggestion, NbtSuggestion.ParentInfo.blank(), true);
				case COMPONENT -> readValue(suggestionList, suggestion, NbtSuggestion.ParentInfo.fromRoot(parentTag), false);
			};
		}
		catch (CommandSyntaxException ignore) { return Suggestion.NONE; }
	}

	private Suggestion readPath(SuggestionList suggestionList) throws CommandSyntaxException
	{
		while (true)
		{
			String key = readPathKey();

			SuggestionList potentialSuggestions = new SuggestionList();
			TagSuggestion tagSuggestion = matchSuggestions(suggestionList, key, potentialSuggestions);
			NbtSuggestion foundSuggestion = tagSuggestion != null ? tagSuggestion.getSourceSuggestion() : null;
			lastFoundSuggestion = foundSuggestion;

			if (!reader.canRead())
			{
				suggestionList.replaceWith(potentialSuggestions);
				//TODO: test and fix it, it will probably be offset with quoted keys (1.4)
				reader.setCursor(reader.getCursor() - key.length());
				return Suggestion.TAG;
			}

			if (foundSuggestion == null) { return Suggestion.NONE; }

			if (reader.peek() == '[')
			{
				expect('[');
				if (!reader.canRead()) { return Suggestion.NONE; }

				if (reader.peek() == '{')
				{
					Suggestion compoundSuggestion = readSubcompound(suggestionList, foundSuggestion, NbtSuggestion.ParentInfo.blank());
					if (compoundSuggestion != Suggestion.CONTINUE) { return compoundSuggestion; }
				}
				else
				{
					reader.readUnquotedString();
				}

				expect(']');
			}

			if (!reader.canRead()) { return Suggestion.NONE; }
			if (reader.peek() == '{')
			{
				Suggestion compoundSuggestion = readSubcompound(suggestionList, foundSuggestion, NbtSuggestion.ParentInfo.blank());
				if (compoundSuggestion != Suggestion.CONTINUE) { return compoundSuggestion; }
			}

			if (!reader.canRead()) { return Suggestion.NONE; }
			expect('.');

			suggestionList.clear();
			suggestionList.addAll(foundSuggestion.subcompound, parserType);
		}
	}

	private Suggestion readStruct(SuggestionList suggestionList, @Nullable NbtSuggestion suggestion,
								  NbtSuggestion.ParentInfo parentInfo, boolean isRoot, boolean isMapScanner) throws CommandSyntaxException
	{
		if (suggestion != null)
		{
			if (!isMapScanner)
			{
				SuggestionList tempSuggestionList = new SuggestionList();
				Map<String, String> tempTagMap = new HashMap<>();
				NbtSuggestion.ParentInfo tempParentInfo = parentInfo.withTagMap(tempTagMap);

				tempSuggestionList.addAll(suggestion.subcompound, parserType);
				suggestion.getSubtypeTagSuggestions(tempSuggestionList, tempParentInfo, parserType);

				int oldPos = reader.getCursor();

				try
				{
					readStruct(tempSuggestionList, suggestion, tempParentInfo, true, true);
				}
				catch (Exception ignore) {}

				suggestion.getSubtypeTagSuggestions(suggestionList, tempParentInfo, parserType);
				reader.setCursor(oldPos);
			}
			else
			{
				suggestion.getSubtypeTagSuggestions(suggestionList, parentInfo, parserType);
			}
		}

		if (!reader.canRead()) { return Suggestion.COMPOUND_BEGIN; }
		expect('{');

		if (reader.canRead() && reader.peek() == '}')
		{
			expect('}');
			return isRoot ? Suggestion.END : Suggestion.CONTINUE;
		}

		do
		{
			reader.skipWhitespace();
			ReadResults readResults = readPartialString();
			String key = readResults.str;

			if (key.isEmpty())
			{
				// sets cursor because it can start with quote
				reader.setCursor(reader.getCursor() - readResults.fullLength());
				return Suggestion.TAG;
			}
			parentInfo.putTag(key, null);

			SuggestionList potentialSuggestions = new SuggestionList();
			TagSuggestion foundSuggestion = matchSuggestions(suggestionList, key, potentialSuggestions);

			if (!reader.canRead())
			{
				if (foundSuggestion != null && readResults.finished) { return Suggestion.COLON; }

				suggestionList.replaceWith(potentialSuggestions);
				reader.setCursor(reader.getCursor() - readResults.fullLength());
				return Suggestion.TAG;
			}

			if (foundSuggestion != null) { suggestionList.remove(foundSuggestion); }

			expect(':');

			NbtSuggestion nbtSuggestion = foundSuggestion != null ? foundSuggestion.getSourceSuggestion() : null;
			Suggestion suggestionForValue = readValue(suggestionList, nbtSuggestion, parentInfo, false);

			if (suggestionForValue != Suggestion.CONTINUE) { return suggestionForValue; }
		} while (readElementSeparator());

		reader.skipWhitespace();
		if (reader.canRead())
		{
			expect('}');
			return isRoot ? Suggestion.END : Suggestion.CONTINUE;
		}
		else
		{
			if (suggestionList.isEmpty()) { return Suggestion.COMPOUND_END; }
			else { return Suggestion.COMMA_OR_COMPOUND_END; }
		}
	}

	private String readPathKey() throws CommandSyntaxException
	{
		if (!reader.canRead()) { return ""; }
		if (reader.peek() == '\'' || reader.peek() == '"') { return reader.readQuotedString(); }

		int start = reader.getCursor();
		while (reader.canRead() && StringReader.isAllowedInUnquotedString(reader.peek()) && reader.peek() != '.')
		{
			reader.skip();
		}
		return reader.getString().substring(start, reader.getCursor());
	}

	private Suggestion readValue(SuggestionList suggestionList, @Nullable NbtSuggestion suggestion,
								 NbtSuggestion.ParentInfo parentInfo, boolean fromList) throws CommandSyntaxException
	{
		reader.skipWhitespace();
		char peek = reader.canRead() ? reader.peek() : '\0';

		if (peek == '{') { return readSubcompound(suggestionList, suggestion, parentInfo); }
		else if (peek == '[') { return readListOrArray(suggestionList, suggestion, parentInfo); }
		else { return readSimpleValue(suggestionList, suggestion, parentInfo, fromList); }
	}

	private Suggestion readSimpleValue(SuggestionList suggestionList, @Nullable NbtSuggestion suggestion,
									   NbtSuggestion.ParentInfo parentInfo, boolean fromList)
	{
		NbtSuggestion.Type expectedType = suggestion != null
				? (fromList ? suggestion.listType : suggestion.type)
				: NbtSuggestion.Type.UNKNOWN;

		reader.skipWhitespace();
		int cursor = reader.getCursor();

		ReadResults stringResults = readPartialString();
		String str = stringResults.str;
		boolean finished = stringResults.finished;

		if (finished && suggestion != null) { parentInfo.putTag(suggestion.tag, str); }

		//TODO: check if checking for string error (!success) is necessary
		//TODO: clean it up
		if (!reader.canRead() || !finished)
		{
			if (expectedType == NbtSuggestion.Type.STRING && stringResults.quoted && finished)
			{
				return Suggestion.CONTINUE;
			}

			reader.setCursor(cursor);
			if (suggestion == null || expectedType == NbtSuggestion.Type.LIST) { return Suggestion.fromNbtType(expectedType); }

			if (suggestion.subtype == NbtSuggestionSubtype.JSON_TEXT
					&& (str.startsWith("{") || str.startsWith("[")) && reader.peek() == '\'')
			{
				Pair<Suggestion, Integer> results = parseJsonComponent(suggestionList, str, true);
				reader.setCursor(cursor + results.getRight() + 1);
				return results.getLeft();
			}
			else if (suggestion.getSubtypeSuggestions(suggestionList, parentInfo, parserType))
			{
				suggestionList.removeIf((s) -> !s.matchUnfinished(str));
				return Suggestion.TAG;
			}

			return Suggestion.fromNbtType(expectedType);
		}

		return Suggestion.CONTINUE;
	}

	private Suggestion readListOrArray(SuggestionList suggestionList, @Nullable NbtSuggestion suggestion,
									   NbtSuggestion.ParentInfo parentInfo) throws CommandSyntaxException
	{
		if (reader.canRead(3) && !StringReader.isQuotedStringStart(reader.peek(1)) && reader.peek(2) == ';')
		{
			readArray();
			return Suggestion.CONTINUE;
		}
		else
		{
			return readList(suggestionList, suggestion, parentInfo, false);
		}
	}

	private Suggestion readList(SuggestionList suggestionList, NbtSuggestion suggestion,
								NbtSuggestion.ParentInfo parentInfo, boolean isRoot) throws CommandSyntaxException
	{
		expect('[');
		reader.skipWhitespace();

		if (reader.canRead() && reader.peek() == ']')
		{
			expect(']');
			return isRoot ? Suggestion.END : Suggestion.CONTINUE;
		}

		do
		{
			Suggestion valueSuggestion = readValue(suggestionList, suggestion, parentInfo, true);
			if (valueSuggestion != Suggestion.CONTINUE) { return valueSuggestion; }
		} while (readElementSeparator());

		if (reader.canRead())
		{
			expect(']');
			return isRoot ? Suggestion.END : Suggestion.CONTINUE;
		}
		else
		{
			return Suggestion.COMMA_OR_LIST_END;
		}
	}

	private void readArray() throws CommandSyntaxException
	{
		expect('[');
		char key = reader.read();
		reader.read();
		reader.skipWhitespace();

		if (!reader.canRead()) { throw TagParser.ERROR_EXPECTED_KEY.createWithContext(reader); }
		else if (key == 'B' || key == 'L' || key == 'I') { readArrayElements(); }
		else { throw TagParser.ERROR_EXPECTED_KEY.createWithContext(reader); }
	}

	private void readArrayElements() throws CommandSyntaxException
	{
		while (true)
		{
			if (reader.peek() != ']')
			{
				readValue(null, null, NbtSuggestion.ParentInfo.blank(), false);

				if (readElementSeparator())
				{
					if (!reader.canRead()) { throw TagParser.ERROR_EXPECTED_VALUE.createWithContext(reader); }
					continue;
				}
			}

			expect(']');
			break;
		}
	}

	private Suggestion readSubcompound(SuggestionList suggestionList, @Nullable NbtSuggestion suggestion,
									   NbtSuggestion.ParentInfo parentInfo) throws CommandSyntaxException
	{
		SuggestionList newSuggestionList = new SuggestionList();
		if (suggestion != null) { newSuggestionList.addAll(suggestion.subcompound, parserType); }
		Suggestion substructSuggestion = readStruct(newSuggestionList, suggestion, parentInfo.createChild(suggestion), false, false);

		if (substructSuggestion == Suggestion.TAG) { suggestionList.replaceWith(newSuggestionList); }
		return substructSuggestion;
	}

	private TagSuggestion matchSuggestions(SuggestionList suggestionList, String key, SuggestionList potentialSuggestions)
	{
		for (CustomSuggestion customSuggestion : suggestionList)
		{
			if (customSuggestion instanceof TagSuggestion && customSuggestion.match(key))
			{
				return (TagSuggestion)customSuggestion;
			}
			if (customSuggestion.matchUnfinished(key)) { potentialSuggestions.add(customSuggestion); }
		}
		return null;
	}

	//TODO: restore and use it?
	/*private @Nullable String readString()
	{
		Pair<String, Boolean> fullOrPartial = readStringFullOrPartial();
		return fullOrPartial.getRight() ? fullOrPartial.getLeft() : null;
	}*/

	private ReadResults readPartialString()
	{
		if (!reader.canRead()) { return new ReadResults("", true, false); }

		char quoteChar = reader.peek();
		if (!StringReader.isQuotedStringStart(quoteChar))
		{
			return new ReadResults(reader.readUnquotedString(), true, false);
		}
		reader.skip();

		StringBuilder stringBuilder = new StringBuilder();
		boolean escaped = false;
		while (reader.canRead())
		{
			char ch = reader.read();

			if (escaped)
			{
				if (ch != quoteChar && ch != '\\') { return new ReadResults(stringBuilder.toString(), false, true); }
				stringBuilder.append(ch);
				escaped = false;
				continue;
			}

			if (ch == quoteChar) { return new ReadResults(stringBuilder.toString(), true, true); }

			if (ch == '\\')
			{
				escaped = true;
				continue;
			}

			stringBuilder.append(ch);
		}
		return new ReadResults(stringBuilder.toString(), false, true);
	}

	private boolean readElementSeparator()
	{
		reader.skipWhitespace();
		if (reader.canRead() && reader.peek() == ',')
		{
			reader.skip();
			reader.skipWhitespace();
			return true;
		}
		return false;
	}

	private void expect(char ch) throws CommandSyntaxException
	{
		reader.skipWhitespace();
		reader.expect(ch);
		reader.skipWhitespace();
	}

	public int getCursor()
	{
		return reader.getCursor();
	}

	public enum Suggestion
	{
		TAG,
		COLON,
		COMPOUND_BEGIN,
		COMPOUND_END,
		COMMA_OR_COMPOUND_END,
		LIST_BEGIN,
		COMMA_OR_LIST_END,
		BOOLEAN,
		STRING,
		BYTE_ARRAY,
		INT_ARRAY,
		LONG_ARRAY,
		TYPE_BYTE,
		TYPE_SHORT,
		TYPE_INT,
		TYPE_LONG,
		TYPE_FLOAT,
		TYPE_DOUBLE,
		JSON_END,
		JSON_BOOLEAN,
		NONE,
		CONTINUE,
		END;

		private static final String[] ALT_BOOLEAN_SUGGESTIONS = new String[]{"1b", "0b"};

		public void suggest(SuggestionsBuilder suggestionsBuilder)
		{
			String typeString = getTypeName();
			if (typeString != null)
			{
				typeString = String.format("[%s]", typeString);
			}

			for (String suggestion : getSuggestions())
			{
				if (suggestion.startsWith(suggestionsBuilder.getRemaining()))
				{
					String type = (!suggestion.isEmpty() && typeString != null) ? (" " + typeString) : typeString;
					NbtSuggestionManager.simpleSuggestion(suggestion, type, suggestionsBuilder);
				}
			}
		}

		private String[] getSuggestions()
		{
			return (this == BOOLEAN && ModConfig.shortBoolean.val) ? ALT_BOOLEAN_SUGGESTIONS : getConstSuggestions();
		}

		private String[] getConstSuggestions()
		{
			return switch (this)
			{
				case COLON -> new String[]{":"};
				case COMPOUND_BEGIN -> new String[]{"{"};
				case COMPOUND_END -> new String[]{"}"};
				case COMMA_OR_COMPOUND_END -> new String[]{",", "}"};
				case LIST_BEGIN -> new String[]{"["};
				case COMMA_OR_LIST_END -> new String[]{",", "]"};
				case STRING -> new String[]{"\""};
				case BYTE_ARRAY -> new String[]{"[B;"};
				case INT_ARRAY -> new String[]{"[I;"};
				case LONG_ARRAY -> new String[]{"[L;"};
				case BOOLEAN, JSON_BOOLEAN -> new String[]{"true", "false"};
				case TYPE_BYTE, TYPE_SHORT, TYPE_INT, TYPE_LONG, TYPE_FLOAT, TYPE_DOUBLE -> new String[]{""};
				case JSON_END -> new String[]{"'"};
				default -> new String[]{};
			};
		}

		private String getTypeName()
		{
			return switch (this)
			{
				case TYPE_BYTE -> NbtSuggestion.Type.BYTE.getName();
				case TYPE_SHORT -> NbtSuggestion.Type.SHORT.getName();
				case TYPE_INT -> NbtSuggestion.Type.INT.getName();
				case TYPE_LONG -> NbtSuggestion.Type.LONG.getName();
				case TYPE_FLOAT -> NbtSuggestion.Type.FLOAT.getName();
				case TYPE_DOUBLE -> NbtSuggestion.Type.DOUBLE.getName();
				case BOOLEAN, JSON_BOOLEAN -> NbtSuggestion.Type.BOOLEAN.getName();
				case STRING -> NbtSuggestion.Type.STRING.getName();
				case BYTE_ARRAY -> NbtSuggestion.Type.BYTE_ARRAY.getName();
				case INT_ARRAY -> NbtSuggestion.Type.INT_ARRAY.getName();
				case LONG_ARRAY -> NbtSuggestion.Type.LONG_ARRAY.getName();
				default -> null;
			};
		}

		public Suggestion asJsonSuggestion(boolean inner)
		{
			return switch (this)
			{
				case BOOLEAN -> JSON_BOOLEAN;
				case END -> inner ? JSON_END : END;
				default -> this;
			};
		}

		public static Suggestion fromNbtType(NbtSuggestion.Type type)
		{
			return switch (type)
			{
				case STRING -> STRING;
				case COMPOUND -> COMPOUND_BEGIN;
				case LIST -> LIST_BEGIN;
				case BOOLEAN -> BOOLEAN;
				case BYTE_ARRAY -> BYTE_ARRAY;
				case LONG_ARRAY -> LONG_ARRAY;
				case BYTE -> TYPE_BYTE;
				case SHORT -> TYPE_SHORT;
				case INT -> TYPE_INT;
				case LONG -> TYPE_LONG;
				case FLOAT -> TYPE_FLOAT;
				case DOUBLE -> TYPE_DOUBLE;
				case INT_ARRAY, UUID -> INT_ARRAY;
				default -> NONE;
			};
		}
	}

	public enum Type
	{
		COMPOUND(false, false),
		PATH(false, true),
		JSON(true, false),
		JSON_LIST(true, false),
		COMPONENT(false, false);

		public final boolean requiresDoubleQuotes;
		public final boolean requiresNamespace;

		Type(boolean requiresDoubleQuotes, boolean requiresNamespace)
		{
			this.requiresDoubleQuotes = requiresDoubleQuotes;
			this.requiresNamespace = requiresNamespace;
		}
	}

	private record ReadResults(String str, boolean finished, boolean quoted)
	{
		public int fullLength()
		{
			return str.length() + (quoted ? 1 : 0);
		}
	}
}