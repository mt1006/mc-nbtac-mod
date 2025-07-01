package net.mt1006.nbtac.autocomplete.loader;

import net.mt1006.nbtac.autocomplete.DataComponentManager;
import net.mt1006.nbtac.autocomplete.NbtSuggestionManager;
import net.mt1006.nbtac.autocomplete.NbtSuggestionMap;
import net.mt1006.nbtac.autocomplete.suggestions.NbtSuggestion;
import net.mt1006.nbtac.autocomplete.type.DynamicArgumentType;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.utils.SimpleStringReader;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SuggestionFileParser
{
	private static final String ROOT_DIR = "/suggestions_v3/";

	public static void parseNbtSuggestions(String filename, String namespace)
	{
		String keyPrefix = filename + "/" + namespace + ":";
		for (Entry entry : parseFile(filename))
		{
			SimpleStringReader reader = new SimpleStringReader(entry.header);
			String entryName = reader.readString();
			reader.expectEnd();

			NbtSuggestionManager.add(keyPrefix + entryName, parseSuggestions(entry.suggestions));
		}
	}

	public static void parseDataComponents(String filename, String namespace)
	{
		String keyPrefix = filename + "/" + namespace + ":";
		for (Entry entry : parseFile(filename))
		{
			SimpleStringReader reader = new SimpleStringReader(entry.header);
			String entryName = reader.readString();
			reader.expect(' ');
			reader.expect(':');
			Type type = parseType(reader);

			NbtSuggestion suggestion = new NbtSuggestion(entryName, type);
			parseAnnotations(reader, suggestion);
			reader.expectEnd();

			suggestion.subcompound = parseSuggestions(entry.suggestions);
			DataComponentManager.componentMap.put(keyPrefix + entryName, suggestion);
		}
	}

	private static List<Entry> parseFile(String filename)
	{
		String path = ROOT_DIR + filename;
		InputStream stream = SuggestionFileParser.class.getResourceAsStream(path);
		if (stream == null) { throw new RuntimeException("Failed to open \"" + path + "\n"); }

		List<Entry> entryList = new ArrayList<>();
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(stream)))
		{
			String line;
			Entry lastEntry = null;

			while ((line = fileReader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#")) { continue; }

				if (line.startsWith("+") || line.startsWith("\t"))
				{
					if (lastEntry == null) { throw new RuntimeException("Failed to parse file \"" + path + "\""); }
					lastEntry.suggestions.add(line);
				}
				else
				{
					lastEntry = new Entry(line);
					entryList.add(lastEntry);
				}
			}
			return entryList;
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

	private static @Nullable NbtSuggestionMap parseSuggestions(List<String> lines)
	{
		Stack<NbtSuggestion> suggestionStack = new Stack<>();
		NbtSuggestionMap outputMap = null;

		for (String line : lines)
		{
			SimpleStringReader reader = new SimpleStringReader(line);

			int tabCount = reader.readTabs();
			reader.expect('+');
			String name = reader.readString();
			reader.expect(' ');
			reader.expect(':');

			Type type = parseType(reader);
			NbtSuggestion suggestion = new NbtSuggestion(name, type);
			parseAnnotations(reader, suggestion);

			if (suggestionStack.size() == tabCount)
			{
				suggestionStack.push(suggestion);
			}
			else if (suggestionStack.size() < tabCount)
			{
				throw reader.new ReaderException();
			}
			else
			{
				suggestionStack.setSize(tabCount + 1);
				suggestionStack.set(tabCount, suggestion);
			}

			if (tabCount == 0)
			{
				if (outputMap == null) { outputMap = new NbtSuggestionMap(); }
				if (!outputMap.add(suggestion)) { throw reader.new ReaderException(); }
			}
			else
			{
				NbtSuggestion nbtSuggestion = suggestionStack.get(tabCount - 1);
				if (nbtSuggestion.subcompound == null) { nbtSuggestion.subcompound = new NbtSuggestionMap(); }
				if (!nbtSuggestion.subcompound.add(suggestion)) { throw reader.new ReaderException(); }
			}
			reader.expectEnd();
		}
		return outputMap;
	}

	private static Type parseType(SimpleStringReader reader)
	{
		String name = reader.readString();

		Type subtype = null;
		if (reader.peek() == '<')
		{
			reader.skipChar();
			subtype = parseType(reader);
			reader.expect('>');
		}

		List<String> args = List.of();
		if (reader.peek() == '(')
		{
			reader.skipChar();
			args = reader.parseList(SimpleStringReader::readString, ')');
		}

		boolean useDynamicType = false;
		for (String arg : args)
		{
			if (arg.contains("$"))
			{
				useDynamicType = true;
				break;
			}
		}

		Type.TypeConstructor constructor = Type.MAP.get(name);
		if (constructor == null) { throw reader.new ReaderException(); }

		return useDynamicType
				? new DynamicArgumentType(constructor, subtype, args)
				: constructor.create(subtype, args);
	}

	private static void parseAnnotations(SimpleStringReader reader, NbtSuggestion suggestion)
	{
		while (reader.peek() == ' ')
		{
			reader.skipChar();
			reader.expect('@');
			String name = reader.readString();

			List<String> args = List.of();
			if (reader.peek() == '(')
			{
				reader.skipChar();
				args = reader.parseList(SimpleStringReader::readString, ')');
			}

			if (!suggestion.addAnnotation(name, args)) { throw reader.new ReaderException(); }
		}
	}

	private static class Entry
	{
		private final String header;
		private final List<String> suggestions = new ArrayList<>();

		private Entry(String header)
		{
			this.header = header;
		}
	}
}
