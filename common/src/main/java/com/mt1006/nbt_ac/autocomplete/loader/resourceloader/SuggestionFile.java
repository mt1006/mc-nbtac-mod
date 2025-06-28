package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.mt1006.nbt_ac.utils.SimpleStringReader;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SuggestionFile
{
	public final String path;
	public final Type type;
	public final Map<String, Type> suggestions;

	private SuggestionFile(String path, Type suggestionType, Map<String, Type> suggestionMap)
	{
		this.path = path;
		this.type = suggestionType;
		this.suggestions = suggestionMap;
	}

	public static SuggestionFile parse(String path, InputStream inputStream)
	{
		Type suggestionType = null;
		Map<String, Type> suggestionMap = new HashMap<>();

		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream)))
		{
			String line;
			boolean firstLine = true;
			Stack<Type> typeStack = new Stack<>();

			while ((line = fileReader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#")) { continue; }

				SimpleStringReader reader = new SimpleStringReader(line);
				if (firstLine)
				{
					reader.expect(':');
					suggestionType = Type.parse(reader);
					firstLine = false;
				}
				else
				{
					int tabCount = reader.readTabs();
					reader.expect('+');
					String name = reader.readString();
					reader.expect(' ');
					reader.expect(':');

					Type type = Type.parse(reader);
					if (typeStack.size() == tabCount)
					{
						typeStack.push(type);
					}
					else if (typeStack.size() < tabCount)
					{
						reader.throwException();
					}
					else
					{
						typeStack.setSize(tabCount + 1);
						typeStack.set(tabCount, type);
					}

					if (tabCount == 0)
					{
						if (suggestionMap.containsKey(name)) { reader.throwException(); }
						suggestionMap.put(name, type);
					}
					else
					{
						if (!typeStack.get(tabCount - 1).addSuggestion(name, type)) { reader.throwException(); }
					}
				}
				reader.expectEnd();
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }

		if (suggestionType == null) { throw new RuntimeException("Suggestion type not specified!"); }
		return new SuggestionFile(path, suggestionType, suggestionMap);
	}

	public static class Type
	{
		public final String name;
		public @Nullable List<Type> subtypes;
		public @Nullable List<String> args;
		public @Nullable List<Annotation> annotations;
		public @Nullable Map<String, Type> suggestions;

		public Type(String name)
		{
			this.name = name;
		}

		private boolean addSuggestion(String name, Type type)
		{
			if (suggestions == null) { suggestions = new HashMap<>(); }
			if (suggestions.containsKey(name)) { return false; }
			suggestions.put(name, type);
			return true;
		}

		private static Type parse(SimpleStringReader reader)
		{
			Type type = new Type(reader.readString());

			if (reader.peek() == '<')
			{
				reader.skipChar();
				type.subtypes = reader.parseList(Type::parse, '>');
			}
			if (reader.peek() == '(')
			{
				reader.skipChar();
				type.args = reader.parseList(SimpleStringReader::readString, ')');
			}

			while (reader.peek() == ' ')
			{
				if (type.annotations == null) { type.annotations = new ArrayList<>(); }
				reader.skipChar();
				reader.expect('@');
				type.annotations.add(Annotation.parse(reader));
			}
			return type;
		}
	}

	public static class Annotation
	{
		public final String name;

		public Annotation(String name)
		{
			this.name = name;
		}

		private static Annotation parse(SimpleStringReader reader)
		{
			return new Annotation(reader.readString());
		}
	}
}
