package com.mt1006.nbt_ac.autocomplete.loader.resourceloader;

import com.mt1006.nbt_ac.NBTac;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuggestionDirReader
{
	// https://stackoverflow.com/a/28057735
	// https://stackoverflow.com/a/37730853
	public static List<SuggestionFile> openDir(String dir)
	{
		URL url = NBTac.class.getResource(dir);
		if (url == null) { throw new RuntimeException("Failed to load suggestion directory " + dir); }

		URI uri;
		try { uri = url.toURI(); }
		catch (URISyntaxException e) { throw new RuntimeException(e); }

		List<SuggestionFile> suggestionFiles = new ArrayList<>();

		if (uri.getScheme().equals("jar"))
		{
			try (FileSystem fileSystem = FileSystems.getFileSystem(uri))
			{
				for (Path path : listDir(fileSystem.getPath(dir)))
				{
					InputStream inputStream = NBTac.class.getResourceAsStream(path.toString());
					if (inputStream == null) { throw new RuntimeException("Failed to open " + path); }
					suggestionFiles.add(SuggestionFile.parse(path.getFileName().toString(), inputStream));
					inputStream.close();
				}
			}
			catch (IOException e) { throw new RuntimeException(e); }
		}
		else
		{
			for (Path path : listDir(Paths.get(uri)))
			{
				try (InputStream inputStream = new FileInputStream(path.toFile()))
				{
					suggestionFiles.add(SuggestionFile.parse(path.getFileName().toString(), inputStream));
				}
				catch (IOException e) { throw new RuntimeException(e); }
			}
		}
		return suggestionFiles;
	}

	private static List<Path> listDir(Path path)
	{
		try (Stream<Path> stream = Files.list(path))
		{
			return stream.collect(Collectors.toList());
		} catch (IOException e) { throw new RuntimeException(e); }
	}
}
