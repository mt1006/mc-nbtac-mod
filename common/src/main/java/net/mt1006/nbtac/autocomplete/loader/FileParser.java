package net.mt1006.nbtac.autocomplete.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public abstract class FileParser
{
	private static final String ROOT_DIR = "/suggestions_v3/";
	private final String data;
	private final boolean dataAsFilename;

	protected FileParser(String data, boolean dataAsFilename)
	{
		this.data = data;
		this.dataAsFilename = dataAsFilename;
	}

	protected List<Entry> parseLines()
	{
		List<String> lines;
		if (!dataAsFilename)
		{
			lines = data.lines().toList();
		}
		else
		{
			String path = ROOT_DIR + data;
			InputStream stream = SuggestionDataParser.class.getResourceAsStream(path);
			if (stream == null) { throw new RuntimeException("Failed to open \"" + path + "\n"); }

			try (Reader reader = new InputStreamReader(stream))
			{
				lines = reader.readAllLines();
			}
			catch (IOException e) { throw new RuntimeException(e); }
		}

		List<Entry> entryList = new ArrayList<>();
		Entry lastEntry = null;
		for (String line : lines)
		{
			if (line.isEmpty() || line.startsWith("#")) { continue; }

			if (line.startsWith("+") || line.startsWith("\t"))
			{
				if (lastEntry == null) { throw new RuntimeException("Failed to parse: " + line); }
				lastEntry.lines.add(line);
			}
			else
			{
				lastEntry = new Entry(line);
				entryList.add(lastEntry);
			}
		}
		return entryList;
	}

	protected static class Entry
	{
		protected final String header;
		protected final List<String> lines = new ArrayList<>();

		private Entry(String header)
		{
			this.header = header;
		}
	}
}
