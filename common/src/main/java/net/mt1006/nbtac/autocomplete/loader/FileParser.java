package net.mt1006.nbtac.autocomplete.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class FileParser
{
	private static final String ROOT_DIR = "/suggestions_v3/";
	protected final String filename;

	protected FileParser(String filename)
	{
		this.filename = filename;
	}

	protected List<Entry> parseFile()
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
				if (line.isEmpty()) { continue; }

				if (line.startsWith("+") || line.startsWith("\t"))
				{
					if (lastEntry == null) { throw new RuntimeException("Failed to parse file \"" + path + "\""); }
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
		catch (IOException e) { throw new RuntimeException(e); }
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
