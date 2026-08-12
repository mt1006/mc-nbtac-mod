package net.mt1006.nbtac.autocomplete.loader;

import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.DataComponentManager;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.tag.DefinedNbtTag;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.autocomplete.type.DynamicArgumentType;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.autocomplete.type.compound.TagsType;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.SimpleStringReader;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

public class SuggestionFileParser extends FileParser
{
	private final String keyDefaultPrefix, keyModPrefix;

	public SuggestionFileParser(String filename, String namespace)
	{
		super(filename);
		this.keyDefaultPrefix = filename + "/" + namespace + ":";
		this.keyModPrefix = "_" + this.keyDefaultPrefix;
	}

	public void parseNbtSuggestions()
	{
		for (Entry entry : parseFile())
		{
			SimpleStringReader reader = new SimpleStringReader(entry.header);
			String entryKey = parseNbtEntryName(reader.readFileString());

			if (reader.peek() != ' ')
			{
				reader.expectEnd();
				NbtTagManager.add(entryKey, parseRequiredSuggestions(entry.lines, null, entryKey));
				continue;
			}
			reader.skipChar(); // skip space

			char sign = reader.peek();
			reader.skipChar();

			String referencedKey = parseNbtEntryName(reader.readFileString());
			NbtTagMap parentTagMap = NbtTagManager.get(referencedKey);
			if (parentTagMap == null) { throw reader.new ReaderException(); }

			switch (sign)
			{
				case '=':
					NbtTagManager.add(entryKey, parentTagMap);
					reader.expectEnd();
					if (!entry.lines.isEmpty()) { throw reader.new ReaderException(); }
					break;

				case '&':
					reader.expectEnd();
					NbtTagMap childTagMap = parseRequiredSuggestions(entry.lines, parentTagMap, entryKey);
					NbtTagManager.add(entryKey, childTagMap);
					break;

				default:
					throw reader.new ReaderException();
			}
		}
	}

	public void parseDataComponents()
	{
		for (Entry entry : parseFile())
		{
			SimpleStringReader reader = new SimpleStringReader(entry.header);
			String entryName = reader.readFileString();
			reader.expect(' ');
			reader.expect(':');
			Type type = parseType(reader);

			DefinedNbtTag tag = new DefinedNbtTag(entryName, type);
			parseAnnotations(reader, tag);
			reader.expectEnd();

			tag.getType().setSubcompound(parseSuggestions(entry.lines, null));
			if (tag.inVersionRange()) { DataComponentManager.componentMap.put(keyDefaultPrefix + entryName, tag); }
		}
	}

	private static NbtTagMap parseRequiredSuggestions(List<String> lines, @Nullable NbtTagMap parent, String entryKey)
	{
		NbtTagMap tagMap = parseSuggestions(lines, parent);
		if (tagMap == null) { throw new RuntimeException("Failed to parse required suggestions for " + entryKey); }
		return tagMap;
	}

	private static @Nullable NbtTagMap parseSuggestions(List<String> lines, @Nullable NbtTagMap parent)
	{
		if (lines.isEmpty()) { return null; }
		Stack<NbtTag> suggestionStack = new Stack<>();
		NbtTagMap outputMap = new NbtTagMap(parent);

		for (String line : lines)
		{
			SimpleStringReader reader = new SimpleStringReader(line);

			int tabCount = reader.readTabs();
			reader.expect('+');
			String name = reader.readFileString();
			reader.expect(' ');
			reader.expect(':');

			Type type = parseType(reader);
			DefinedNbtTag tag = new DefinedNbtTag(name, type);
			parseAnnotations(reader, tag);

			if (suggestionStack.size() == tabCount)
			{
				suggestionStack.push(tag);
			}
			else if (suggestionStack.size() < tabCount)
			{
				throw reader.new ReaderException();
			}
			else
			{
				suggestionStack.setSize(tabCount + 1);
				suggestionStack.set(tabCount, tag);
			}

			if (tag.inVersionRange())
			{
				if (tabCount == 0)
				{
					if (!outputMap.add(tag)) { throw reader.new ReaderException(); }
				}
				else
				{
					NbtTag nbtTag = suggestionStack.get(tabCount - 1);
					if (!nbtTag.getType().getSubcompound().add(tag)) { throw reader.new ReaderException(); }
				}
			}
			reader.expectEnd();
		}
		return outputMap;
	}

	private static Type parseType(SimpleStringReader reader)
	{
		if (reader.peek() == '#')
		{
			reader.skipChar(); // skip '#'

			String id = "compound/nbtac:" + reader.readFileString();
			if (ModConfig.debugMode.val && NbtTagManager.get(id) == null) { NBTac.LOGGER.warn("{} not found", id); }
			return new TagsType(id, null, null);
		}

		String name = reader.readFileString();
		List<Type> subtypes = reader.parseList(SuggestionFileParser::parseType, '<', '>');
		List<String> args = reader.parseList(SimpleStringReader::readFileString, '(', ')');

		int firstDynamicArg = -1;
		for (int i = 0; i < args.size(); i++)
		{
			if (args.get(i).contains("$"))
			{
				firstDynamicArg = i;
				break;
			}
		}

		Type.TypeConstructor constructor = Type.MAP.get(name);
		if (constructor == null) { throw reader.new ReaderException(); }

		return firstDynamicArg != -1
				? new DynamicArgumentType(constructor, subtypes, args, firstDynamicArg)
				: constructor.create(subtypes, args);
	}

	private static void parseAnnotations(SimpleStringReader reader, DefinedNbtTag tag)
	{
		while (reader.peek() == ' ')
		{
			reader.skipChar(); // skip space
			reader.expect('@');
			String name = reader.readFileString();
			List<String> args = reader.parseList(SimpleStringReader::readFileString, '(', ')');

			if (!tag.addAnnotation(name, args)) { throw reader.new ReaderException(); }
		}
	}

	private String parseNbtEntryName(String str)
	{
		return (str.startsWith("_") ? keyModPrefix : keyDefaultPrefix) + str;
	}
}
