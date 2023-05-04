package com.mt1006.nbt_ac.autocomplete;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.nbt.JsonToNBT;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomTagParser
{
	public final StringReader reader;
	public NbtSuggestion lastFoundSuggestion = null;

	public CustomTagParser(String tag)
	{
		reader = new StringReader(tag);
	}

	public Suggestion read(List<CustomSuggestion> suggestionList, @Nullable NbtSuggestion suggestion,
						   @Nullable String parentTag, boolean suggestPath)
	{
		try
		{
			if (suggestPath)
			{
				return readPath(suggestionList);
			}
			else
			{
				return readStruct(suggestionList, suggestion, NbtSuggestion.ParentInfo.fromRoot(parentTag), false);
			}
		}
		catch (CommandSyntaxException ignore) { return Suggestion.NONE; }
	}

	private Suggestion readPath(List<CustomSuggestion> suggestionList)
	{
		try
		{
			while (true)
			{
				String key = readPathKey();
				if (key.isEmpty()) { return Suggestion.TAG; }

				List<CustomSuggestion> potentialSuggestions = new ArrayList<>();
				NbtSuggestion foundSuggestion = findSuggestion(suggestionList, key, potentialSuggestions);
				lastFoundSuggestion = foundSuggestion;

				if (!reader.canRead())
				{
					suggestionList.clear();
					suggestionList.addAll(potentialSuggestions);
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
						Suggestion compoundSuggestion = readSubcompound(foundSuggestion, suggestionList, NbtSuggestion.ParentInfo.blank());
						if (compoundSuggestion != Suggestion.CONTINUE) { return compoundSuggestion; }
					}
					else
					{
						readPathKey();
					}

					expect(']');
				}

				if (!reader.canRead()) { return Suggestion.NONE; }
				if (reader.peek() == '{')
				{
					Suggestion compoundSuggestion = readSubcompound(foundSuggestion, suggestionList, NbtSuggestion.ParentInfo.blank());
					if (compoundSuggestion != Suggestion.CONTINUE) { return compoundSuggestion; }
				}

				if (!reader.canRead()) { return Suggestion.NONE; }
				expect('.');

				suggestionList.clear();
				if (foundSuggestion.subcompound != null)
				{
					suggestionList.addAll(foundSuggestion.subcompound.getAll());
				}
			}
		}
		catch (CommandSyntaxException ignore) { return Suggestion.NONE; }
	}

	private Suggestion readStruct(List<CustomSuggestion> suggestionList, @Nullable NbtSuggestion suggestion,
								  NbtSuggestion.ParentInfo parentInfo, boolean isMapScanner) throws CommandSyntaxException
	{
		if (suggestion != null)
		{
			if (!isMapScanner)
			{
				List<CustomSuggestion> tempSuggestionList = new ArrayList<>();
				Map<String, String> tempTagMap = new HashMap<>();
				NbtSuggestion.ParentInfo tempParentInfo = parentInfo.withTagMap(tempTagMap);

				if (suggestion.subcompound != null) { tempSuggestionList.addAll(suggestion.subcompound.getAll()); }
				suggestion.getSubtypeTagSuggestions(tempSuggestionList, tempParentInfo);

				int oldPos = reader.getCursor();

				try
				{
					readStruct(tempSuggestionList, suggestion, tempParentInfo, true);
				}
				catch (Exception ignore) {}

				suggestion.getSubtypeTagSuggestions(suggestionList, tempParentInfo);

				reader.setCursor(oldPos);
			}
			else
			{
				suggestion.getSubtypeTagSuggestions(suggestionList, parentInfo);
			}
		}

		if (reader.canRead()) { expect('{'); }
		else { return Suggestion.COMPOUND_BEGIN; }
		reader.skipWhitespace();

		if (reader.canRead() && reader.peek() == '}')
		{
			expect('}');
			return Suggestion.CONTINUE;
		}

		do
		{

			String key = readKey();
			if (key.isEmpty()) { return Suggestion.TAG; }
			parentInfo.putTag(key, null);

			List<CustomSuggestion> potentialSuggestions = new ArrayList<>();
			NbtSuggestion foundSuggestion = findSuggestion(suggestionList, key, potentialSuggestions);

			if (!reader.canRead())
			{
				if (foundSuggestion == null)
				{
					suggestionList.clear();
					suggestionList.addAll(potentialSuggestions);
					reader.setCursor(reader.getCursor() - key.length());
					return Suggestion.TAG;
				}
				else
				{
					return Suggestion.COLON;
				}
			}

			if (foundSuggestion != null) { suggestionList.remove(foundSuggestion); }

			expect(':');
			Suggestion suggestionForValue = readValue(foundSuggestion, suggestionList, parentInfo, false);

			if (suggestionForValue != Suggestion.CONTINUE) { return suggestionForValue; }
		} while (hasElementSeparator());

		reader.skipWhitespace();
		if (reader.canRead())
		{
			expect('}');
			return Suggestion.CONTINUE;
		}
		else
		{
			if (suggestionList.isEmpty()) { return Suggestion.COMPOUND_END; }
			else { return Suggestion.COMMA_OR_COMPOUND_END; }
		}
	}

	private String readKey() throws CommandSyntaxException
	{
		reader.skipWhitespace();
		return reader.readString();
	}

	private String readPathKey()
	{
		reader.skipWhitespace();
		int start = reader.getCursor();

		while (reader.canRead() && StringReader.isAllowedInUnquotedString(reader.peek()) && reader.peek() != '.')
		{
			reader.skip();
		}

		return reader.getString().substring(start, reader.getCursor());
	}

	private Suggestion readValue(@Nullable NbtSuggestion suggestion, List<CustomSuggestion> suggestionList,
								 NbtSuggestion.ParentInfo parentInfo, boolean fromList) throws CommandSyntaxException
	{
		reader.skipWhitespace();
		char peek = reader.canRead() ? reader.peek() : '\0';

		if (peek == '{') { return readSubcompound(suggestion, suggestionList, parentInfo); }
		else if (peek == '[') { return readList(suggestion, suggestionList, parentInfo); }
		else { return readTypedValue(suggestion, suggestionList, parentInfo, fromList); }
	}

	private Suggestion readTypedValue(@Nullable NbtSuggestion suggestion, List<CustomSuggestion> suggestionList,
									  NbtSuggestion.ParentInfo parentInfo, boolean fromList)
	{
		NbtSuggestion.Type expectedType = NbtSuggestion.Type.UNKNOWN;
		if (suggestion != null)
		{
			if (fromList) { expectedType = suggestion.listType; }
			else { expectedType = suggestion.type; }
		}

		reader.skipWhitespace();

		int cursor = reader.getCursor();
		boolean readStringError = false;

		if (reader.canRead())
		{
			if (StringReader.isQuotedStringStart(reader.peek()))
			{
				try
				{
					if (suggestion != null) { parentInfo.putTag(suggestion.tag, reader.readQuotedString()); }
				}
				catch (Exception exception) { readStringError = true; }
			}
			else
			{
				reader.readUnquotedString();
			}
		}

		if (!reader.canRead() || readStringError)
		{
			reader.setCursor(cursor);
			if (suggestion != null && expectedType != NbtSuggestion.Type.LIST &&
					suggestion.getSubtypeSuggestions(suggestionList, parentInfo))
			{
				String prefix = reader.getString().substring(cursor);
				suggestionList.removeIf(customSuggestion ->
						!customSuggestion.getSuggestionText().startsWith(prefix) &&
						!customSuggestion.getSuggestionText().startsWith("\"minecraft:" + prefix) &&
						!(prefix.startsWith("\"") && customSuggestion.getSuggestionText().startsWith("\"minecraft:" + prefix.substring(1))));
				return Suggestion.TAG;
			}
			else
			{
				return Suggestion.fromNbtType(expectedType);
			}
		}

		return Suggestion.CONTINUE;
	}

	private Suggestion readList(@Nullable NbtSuggestion suggestion, List<CustomSuggestion> suggestionList,
								NbtSuggestion.ParentInfo parentInfo) throws CommandSyntaxException
	{
		if (reader.canRead(3) && !StringReader.isQuotedStringStart(reader.peek(1)) && reader.peek(2) == ';')
		{
			readArrayTag();
			if (reader.canRead()) { return Suggestion.CONTINUE; }
			else { return Suggestion.NONE; }
		}
		else
		{
			return readListTag(suggestion, suggestionList, parentInfo);
		}
	}

	private Suggestion readListTag(NbtSuggestion suggestion, List<CustomSuggestion> suggestionList,
								   NbtSuggestion.ParentInfo parentInfo) throws CommandSyntaxException
	{
		expect('[');
		reader.skipWhitespace();

		if (reader.canRead() && reader.peek() == ']')
		{
			expect(']');
			return Suggestion.CONTINUE;
		}

		do
		{
			Suggestion valueSuggestion = readValue(suggestion, suggestionList, parentInfo, true);
			if (valueSuggestion != Suggestion.CONTINUE) { return valueSuggestion; }
		} while (hasElementSeparator());

		if (reader.canRead())
		{
			expect(']');
			return Suggestion.CONTINUE;
		}
		else
		{
			return Suggestion.COMMA_OR_LIST_END;
		}
	}

	private void readArrayTag() throws CommandSyntaxException
	{
		expect('[');
		char key = reader.read();
		reader.read();
		reader.skipWhitespace();

		if (!reader.canRead()) { throw JsonToNBT.ERROR_EXPECTED_KEY.createWithContext(reader); }
		else if (key == 'B' || key == 'L' || key == 'I') { readArray(); }
		else { throw JsonToNBT.ERROR_EXPECTED_KEY.createWithContext(reader); }
	}

	private void readArray() throws CommandSyntaxException
	{
		while (true)
		{
			if (reader.peek() != ']')
			{
				readValue(null, null, NbtSuggestion.ParentInfo.blank(), false);

				if (hasElementSeparator())
				{
					if (!reader.canRead()) { throw JsonToNBT.ERROR_EXPECTED_VALUE.createWithContext(reader); }
					continue;
				}
			}

			expect(']');
			break;
		}
	}

	private Suggestion readSubcompound(@Nullable NbtSuggestion suggestion, List<CustomSuggestion> suggestionList,
									   NbtSuggestion.ParentInfo parentInfo) throws CommandSyntaxException
	{
		List<CustomSuggestion> newSuggestionList = new ArrayList<>();
		if (suggestion != null && suggestion.subcompound != null) { newSuggestionList.addAll(suggestion.subcompound.getAll()); }
		Suggestion substructSuggestion = readStruct(newSuggestionList, suggestion, parentInfo.createChild(suggestion), false);

		if (substructSuggestion == Suggestion.TAG && suggestionList != null)
		{
			suggestionList.clear();
			suggestionList.addAll(newSuggestionList);
		}
		return substructSuggestion;
	}

	private NbtSuggestion findSuggestion(List<CustomSuggestion> suggestionList, String key, List<CustomSuggestion> potentialSuggestions)
	{
		NbtSuggestion foundSuggestion = null;

		for (CustomSuggestion customSuggestion : suggestionList)
		{
			if (customSuggestion instanceof NbtSuggestion)
			{
				NbtSuggestion suggestion = (NbtSuggestion)customSuggestion;
				if (!suggestion.tag.startsWith(key)) { continue; }

				if (suggestion.tag.equals(key))
				{
					foundSuggestion = suggestion;
				}
				potentialSuggestions.add(suggestion);
			}
			else
			{
				potentialSuggestions.add(customSuggestion);
			}
		}

		return foundSuggestion;
	}

	private boolean hasElementSeparator()
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

	private void expect(char p_129353_) throws CommandSyntaxException
	{
		reader.skipWhitespace();
		reader.expect(p_129353_);
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
		NONE,
		CONTINUE;

		public void suggest(SuggestionsBuilder suggestionsBuilder)
		{
			String typeString = getType();
			if (typeString != null)
			{
				typeString = String.format("§8[%s]", typeString);
			}

			for (String suggestion : getSuggestions())
			{
				if (suggestion.startsWith(suggestionsBuilder.getRemaining()))
				{
					String type = null;
					if (typeString != null)
					{
						if (suggestion.isEmpty()) { type = typeString; }
						else { type = " " + typeString; }
					}

					NbtSuggestionManager.simpleSuggestion(suggestion, type, suggestionsBuilder);
				}
			}
		}

		private String[] getSuggestions()
		{
			switch (this)
			{
				case COLON: return new String[]{":"};
				case COMPOUND_BEGIN: return new String[]{"{"};
				case COMPOUND_END: return new String[]{"}"};
				case COMMA_OR_COMPOUND_END: return new String[]{",", "}"};
				case LIST_BEGIN: return new String[]{"["};
				case COMMA_OR_LIST_END: return new String[]{",", "]"};
				case STRING: return new String[]{"\""};
				case BYTE_ARRAY: return new String[]{"[B;"};
				case INT_ARRAY: return new String[]{"[I;"};
				case LONG_ARRAY: return new String[]{"[L;"};
				case BOOLEAN:
					if (ModConfig.shortBoolean.getValue()) { return new String[]{"1b","0b"}; }
					else { return new String[]{"true","false"}; }
				case TYPE_BYTE:
				case TYPE_SHORT:
				case TYPE_INT:
				case TYPE_LONG:
				case TYPE_FLOAT:
				case TYPE_DOUBLE:
					return new String[]{""};
			}

			return new String[]{};
		}

		private String getType()
		{
			switch (this)
			{
				case TYPE_BYTE: return NbtSuggestion.Type.BYTE.getName();
				case TYPE_SHORT: return NbtSuggestion.Type.SHORT.getName();
				case TYPE_INT: return NbtSuggestion.Type.INT.getName();
				case TYPE_LONG: return NbtSuggestion.Type.LONG.getName();
				case TYPE_FLOAT: return NbtSuggestion.Type.FLOAT.getName();
				case TYPE_DOUBLE: return NbtSuggestion.Type.DOUBLE.getName();
				case BOOLEAN: return NbtSuggestion.Type.BOOLEAN.getName();
				case STRING: return NbtSuggestion.Type.STRING.getName();
				case BYTE_ARRAY: return NbtSuggestion.Type.BYTE_ARRAY.getName();
				case INT_ARRAY: return NbtSuggestion.Type.INT_ARRAY.getName();
				case LONG_ARRAY: return NbtSuggestion.Type.LONG_ARRAY.getName();
			}
			return null;
		}

		public static Suggestion fromNbtType(NbtSuggestion.Type type)
		{
			switch (type)
			{
				case STRING: return STRING;
				case COMPOUND: return COMPOUND_BEGIN;
				case LIST: return LIST_BEGIN;
				case BOOLEAN: return BOOLEAN;
				case BYTE_ARRAY: return BYTE_ARRAY;
				case LONG_ARRAY: return LONG_ARRAY;
				case BYTE: return TYPE_BYTE;
				case SHORT: return TYPE_SHORT;
				case INT: return TYPE_INT;
				case LONG: return TYPE_LONG;
				case FLOAT: return TYPE_FLOAT;
				case DOUBLE: return TYPE_DOUBLE;

				case INT_ARRAY:
				case UUID:
					return INT_ARRAY;
			}

			return NONE;
		}
	}
}