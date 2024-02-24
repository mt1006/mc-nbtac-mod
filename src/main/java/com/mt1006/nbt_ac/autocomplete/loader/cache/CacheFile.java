package com.mt1006.nbt_ac.autocomplete.loader.cache;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CacheFile
{
	private static final String END_SEQUENCE = "###END###";

	public static boolean load(File file, String desiredId)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String id = reader.readLine();
			if (!id.equals(desiredId))
			{
				NBTac.LOGGER.warn("Hash matches, but not id - corrupted file, error or hash collision");
				return false;
			}
			return parseFile(reader);
		}
		catch (Exception exception)
		{
			NBTac.LOGGER.error("Failed to load cache file!");
			Loader.printStackTrace(exception);
			return false;
		}
	}

	private static boolean parseFile(BufferedReader reader) throws Exception
	{
		String line;

		if ((line = reader.readLine()) == null) { return false; }
		int lineCount = Integer.parseInt(line);
		if (lineCount < 0) { return false; }

		ParsedLine[] lines = new ParsedLine[lineCount];
		for (int i = 0; i < lineCount; i++)
		{
			line = reader.readLine();
			if (line == null) { return false; }
			lines[i] = new ParsedLine(line);
		}

		if ((line = reader.readLine()) == null) { return false; }
		SuggestionStack stack = new SuggestionStack();
		int len = line.length(), pos = 0;

		for (int i = 0; i < len; i++)
		{
			if (line.charAt(i) != ';') { continue; }
			int id = Integer.parseInt(line, pos, i, 36);
			pos = i + 1;
			ParsedLine parsedLine = lines[id];

			if (parsedLine.depth == 0)
			{
				NbtSuggestions suggestions = new NbtSuggestions();
				NbtSuggestionManager.add(parsedLine.tag, suggestions);
				stack.addRoot(suggestions);
			}
			else
			{
				if (!stack.add(parsedLine.getSuggestion(), parsedLine.depth)) { return false; }
			}
		}

		if ((line = reader.readLine()) == null) { return false; }
		return line.equals(END_SEQUENCE);
	}

	public static void save(File file, String id)
	{
		try (PrintWriter writer = new PrintWriter(file))
		{
			Builder builder = new Builder();

			for (Map.Entry<String, NbtSuggestions> suggestions : NbtSuggestionManager.suggestionMap.entrySet())
			{
				builder.add(suggestions.getKey());
				saveSuggestions(builder, suggestions.getValue(), 1);
			}

			writer.println(id);
			builder.finish(writer);
			writer.print(END_SEQUENCE);
		}
		catch (Exception exception)
		{
			NBTac.LOGGER.error("Failed to save cache file");
			Loader.printStackTrace(exception);
		}
	}

	private static void saveSuggestions(Builder builder, NbtSuggestions suggestions, int depth)
	{
		for (NbtSuggestion suggestion : suggestions.getAll())
		{
			builder.add(packToLine(suggestion, depth));
			if (suggestion.subcompound != null)
			{
				saveSuggestions(builder, suggestion.subcompound, depth + 1);
			}
		}
	}

	private static String packToLine(NbtSuggestion suggestion, int depth)
	{
		String tag = suggestion.tag;
		int type = suggestion.type.ordinal();
		int listType = suggestion.listType.ordinal();
		int suggestionType = suggestion.suggestionType.ordinal();

		return String.format("%s%d;%d;%d#%s", "-".repeat(depth), type, listType, suggestionType, tag);
	}

	private static class SuggestionStack
	{
		private final ArrayList<NbtSuggestions> stack = new ArrayList<>();
		private NbtSuggestion lastSuggestion = null;

		public void addRoot(NbtSuggestions suggestions)
		{
			stack.clear();
			stack.add(suggestions);
		}

		public boolean add(NbtSuggestion suggestion, int depth)
		{
			int currentDepth = stack.size() - 1;
			if (depth > currentDepth + 1) { return false; }

			NbtSuggestions suggestions = stack.get(depth - 1);
			if (suggestions == null)
			{
				suggestions = new NbtSuggestions();
				stack.set(depth - 1, suggestions);
				lastSuggestion.subcompound = suggestions;
			}

			suggestions.add(suggestion);
			if (depth == currentDepth + 1) { stack.add(null); }
			else if (depth < currentDepth) { stack.subList(depth, currentDepth).clear(); }

			lastSuggestion = suggestion;
			return true;
		}
	}

	private static class ParsedLine
	{
		private final String tag;
		private final NbtSuggestion.Type type, listType;
		private final NbtSuggestion.SuggestionType suggestionType;
		public final int depth;

		public ParsedLine(String line) throws Exception
		{
			int calcDepth = 0;
			for (int i = 0; i < line.length(); i++)
			{
				if (line.charAt(i) != '-') { break; }
				calcDepth++;
			}
			depth = calcDepth;

			if (depth == 0)
			{
				tag = line;
				type = null;
				listType = null;
				suggestionType = null;
				return;
			}

			int separator = line.indexOf("#");
			String sizesStr = line.substring(depth, separator);
			String[] values = sizesStr.split(";");
			if (values.length != 3) { throw new Exception(); }

			try
			{
				tag = line.substring(separator + 1);
				type = NbtSuggestion.Type.fromOrdinal(Integer.parseInt(values[0]));
				listType = NbtSuggestion.Type.fromOrdinal(Integer.parseInt(values[1]));
				suggestionType = NbtSuggestion.SuggestionType.fromOrdinal(Integer.parseInt(values[2]));
			}
			catch (Exception exception) { throw new Exception(); }
		}

		public NbtSuggestion getSuggestion()
		{
			return new NbtSuggestion(tag, type, suggestionType, listType);
		}
	}

	private static class Builder
	{
		private final Map<String, Integer> lineMap = new HashMap<>();
		private final StringBuilder stringBuilder = new StringBuilder();
		private int lineCount = 0;

		public void add(String line)
		{
			Integer id = lineMap.get(line);
			int finalId;

			if (id == null)
			{
				lineMap.put(line, lineCount);
				finalId = lineCount++;
			}
			else
			{
				finalId = id;
			}

			stringBuilder.append(Integer.toString(finalId, 36));
			stringBuilder.append(';');
		}

		public void finish(PrintWriter writer)
		{
			writer.println(lineCount);

			String[] lines = new String[lineCount];
			lineMap.forEach((key, value) -> lines[value] = key);
			for (int i = 0; i < lineCount; i++) { writer.println(lines[i]); }

			writer.println(stringBuilder);
		}
	}
}
