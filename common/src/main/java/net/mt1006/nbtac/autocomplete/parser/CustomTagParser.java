package net.mt1006.nbtac.autocomplete.parser;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.autocomplete.type.ListType;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.autocomplete.type.compound.CompoundType;
import net.mt1006.nbtac.autocomplete.type.compound.EntityCompoundType;
import net.mt1006.nbtac.utils.SimpleStringReader;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomTagParser
{
	private final SimpleStringReader reader;
	private final ParserType parserType;
	private final Type rootType;
	private final @Nullable String dataComponentItemId;
	public @Nullable Type parsedPathType = null;
	private boolean success = false; // not set by path parser

	private CustomTagParser(String str, ParserType parserType, Type rootType, @Nullable String dataComponentItemId)
	{
		this.reader = new SimpleStringReader(str, parserType == ParserType.JSON);
		this.parserType = parserType;
		this.rootType = rootType;
		this.dataComponentItemId = dataComponentItemId;
	}

	public static CustomTagParser forValueOfType(String str, Type valueType)
	{
		return new CustomTagParser(str, ParserType.VALUE, valueType, null);
	}

	public static CustomTagParser forNbtCompound(String str, CompoundType compoundType)
	{
		return new CustomTagParser(str, ParserType.VALUE, compoundType, null);
	}

	public static CustomTagParser forNbtPath(String str, CompoundType compoundType)
	{
		return new CustomTagParser(str, ParserType.PATH, compoundType, null);
	}

	public static CustomTagParser forDataComponentValue(String str, Type type, @Nullable ResourceLocation itemId)
	{
		return new CustomTagParser(str, ParserType.VALUE, type, itemId != null ? itemId.toString() : "air");
	}

	public static CustomTagParser forJson(String str, Type type)
	{
		return new CustomTagParser(str, ParserType.JSON, type, null);
	}

	public SuggestionList parse()
	{
		success = false;
		ParsedValue val = null;
		try
		{
			if (parserType == ParserType.PATH)
			{
				val = new ParsedCompound(null, 0);
				optAddVirtualEntityId(val);
				return parsePath((ParsedCompound)val, rootType);
			}
			else if (dataComponentItemId == null)
			{
				val = getNextValue(null, 0);
				optAddVirtualEntityId(val);
				parseValue(val);
			}
			else
			{
				val = getNextValue(ParsedTag.createVirtualItemParents(dataComponentItemId), 0);
				parseValue(val);

				// if val is primitive then throw exception because we don't know if it's finished
				// so we should provide suggestions for it anyway (e.g. for boolean)
				if (val instanceof ParsedPrimitive) { throw reader.new ReaderException(); }
			}

			success = true;
			return SuggestionList.empty();
		}
		catch (SimpleStringReader.ReaderException e)
		{
			SuggestionList list = rootType.getSuggestions(new Type.SuggestionListContext(val, parserType, reader, e.asSuggestionList()));
			return list != null ? list : SuggestionList.empty();
		}
	}

	private void optAddVirtualEntityId(ParsedValue val)
	{
		if (rootType instanceof EntityCompoundType rootCompoundType
				&& rootCompoundType.entityId != null
				&& val instanceof ParsedCompound rootCompound)
		{
			rootCompound.add(ParsedTag.createVirtualId(rootCompound, rootCompoundType.entityId.toString()), -1);
		}
	}

	private SuggestionList parsePath(ParsedCompound compound, Type tagType)
	{
		NbtTagMap tagMap = tagType.getSubcompound();
		boolean inInnerCompound = false;

		try
		{
			while (true)
			{
				ParsedTag tag = compound.add(new ParsedTag(compound), reader.getCursor());
				if (reader.peek() == '\0') { tag.key = ""; }
				else { reader.readNbtPathString((results) -> tag.key = results.str); }

				boolean usedAsList = false, usedAsCompound = false;

				if (reader.peek() == '[')
				{
					tag.val = new ParsedList(tag, reader.getCursor());
					reader.skipChar(); // '['
					reader.readNbtString((results) -> {});
					reader.expect(']');

					usedAsList = true;
				}

				ParsedCompound subcompound = new ParsedCompound(tag, reader.getCursor());
				if (usedAsList) { ((ParsedList)tag.val).add(subcompound, reader.getCursor()); }
				else { tag.val = subcompound; }

				if (reader.peek() == '{')
				{
					inInnerCompound = true;
					parseCompound(subcompound);
					inInnerCompound = false;
					usedAsCompound = true;
				}

				NbtTag nbtTag = tagMap != null ? tagMap.get(tag.key) : null;
				parsedPathType = nbtTag != null ? nbtTag.getType() : null;

				if (reader.peek() == '\0')
				{
					if (tagMap == null) { return SuggestionList.empty(); }

					if (nbtTag == null)
					{
						return (usedAsCompound || usedAsList)
								? SuggestionList.empty()
								: tagMap.suggestionsForKeyPrefix(parserType, compound, tag.key, compound.getLastPos());
					}
					else
					{
						Type elementType = nbtTag.getType();

						SuggestionList suggestions = new SuggestionList(reader.getCursor());
						if (elementType.getPrimitive().isListOrArray())
						{
							if (!usedAsList) { return suggestions.withOperators("["); }
							if (elementType instanceof ListType) { elementType = ((ListType)elementType).getElementType(); }
						}

						if (elementType.getPrimitive() == PrimitiveType.COMPOUND)
						{
							if (!usedAsCompound) { suggestions.withOperators("{"); }
							return suggestions.withOperators(".");
						}
						else
						{
							return SuggestionList.empty();
						}
					}
				}
				else if (reader.peek() == '.')
				{
					compound = (ParsedCompound)(usedAsList ? ((ParsedList)tag.val).getLast() : tag.val);
					tagMap = parsedPathType != null ? parsedPathType.getSubcompound() : null;
					reader.skipChar();
				}
				else
				{
					return SuggestionList.empty();
				}
			}
		}
		catch (SimpleStringReader.ReaderException e)
		{
			if (inInnerCompound) { throw e; }
			return SuggestionList.empty();
		}
	}

	private void parseCompound(ParsedCompound out)
	{
		parseCollection(out, (pos) -> new ParsedTag(out), this::parseNbtTag, '{', '}', false);
	}

	private void parseListOrArray(ParsedCollection<ParsedValue> out, boolean isArray)
	{
		parseCollection(out, (pos) -> getNextValue(null, pos), this::parseValue, '[', ']', isArray);
	}

	private <T extends ParsedCollection<E>, E> void parseCollection(T out, Function<Integer, E> nextValue, Consumer<E> parser,
																	char startSign, char stopSign, boolean isArray)
	{
		reader.expect(startSign);
		if (isArray)
		{
			if (reader.peek() < 'A' || reader.peek() > 'Z') { throw reader.new ReaderException(); }
			reader.skipChar();
			reader.expect(';');
		}
		reader.skipSpaces();

		if (reader.peek() == stopSign)
		{
			reader.skipChar();
			out.close();
			return;
		}

		while (true)
		{
			reader.skipSpaces();
			parser.accept(out.add(nextValue.apply(reader.getCursor()), reader.getCursor()));
			reader.skipSpaces();

			if (reader.biExpect(stopSign, ','))
			{
				out.close();
				return;
			}
		}
	}

	public void parseNbtTag(ParsedTag out)
	{
		reader.readNbtString((results) -> out.key = results.str);
		reader.skipSpaces();
		reader.expect(':');
		reader.skipSpaces();

		out.val = getNextValue(out, reader.getCursor());
		parseValue(out.val);
	}

	public void parseValue(ParsedValue element)
	{
		switch (element)
		{
			case ParsedCompound compound -> parseCompound(compound);
			case ParsedList list -> parseListOrArray(list, false);
			case ParsedArray array -> parseListOrArray(array, true);
			case ParsedPrimitive primitive -> reader.readNbtString(primitive::setFromReader);
			default -> throw new IllegalStateException("Unexpected value: " + element);
		}
	}

	private ParsedValue getNextValue(@Nullable ParsedTag parentTag, int cursorPos)
	{
		switch (reader.peek())
		{
			case '{':
				return new ParsedCompound(parentTag, cursorPos);
			case '[':
				String str = reader.peekSubstring(3);
				return (str.length() == 3 && str.charAt(2) == ';' && str.charAt(1) >= 'A' && str.charAt(1) <= 'Z' && parserType != ParserType.JSON)
						? new ParsedArray(parentTag, cursorPos) : new ParsedList(parentTag, cursorPos);
			default:
				return new ParsedPrimitive(parentTag, cursorPos);
		}
	}

	public boolean wasSuccessful()
	{
		if (parserType == ParserType.PATH) { throw new UnsupportedOperationException(); }
		return success;
	}
}
