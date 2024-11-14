package com.mt1006.nbt_ac.autocomplete.loader.cache;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.config.ModConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class CacheIndex
{
	private final File file;
	private final List<Element> elements = new ArrayList<>();
	private final Map<String, Element> elementMap = new HashMap<>();

	public CacheIndex(File file)
	{
		this.file = file;
		try (Scanner scanner = new Scanner(file))
		{
			int i = 0;
			while (scanner.hasNextLine())
			{
				Element element = new Element(scanner.nextLine(), i++);
				elements.add(element);
				elementMap.put(element.hash, element);
			}
		}
		catch (Exception exception)
		{
			if (exception instanceof FileNotFoundException) { return; }
			
			NBTac.LOGGER.error("Exception while loading cache index!");
			Loader.printStackTrace(exception);
			clear();
		}
	}
	
	public void clear()
	{
		elements.clear();
		elementMap.clear();
	}

	public boolean findAndLoad(File directory, String id, String idHash)
	{
		Element element = elementMap.get(idHash);
		if (element == null) { return false; }

		File file = getFile(directory, element.pos);
		if (!CacheFile.load(file, id)) { return false; }

		element.timestamp = System.currentTimeMillis();
		return true;
	}

	public void save()
	{
		try (PrintWriter writer = new PrintWriter(file))
		{
			elements.forEach((e) -> writer.println(e.str()));
		}
		catch (Exception exception)
		{
			NBTac.LOGGER.error("Exception while saving cache index!");
			Loader.printStackTrace(exception);
		}
	}

	public void add(String idHash, int elementPos)
	{
		Element newElement = new Element(idHash, System.currentTimeMillis(), elementPos);
		
		if (elementPos < elements.size())
		{
			if (elementMap.remove(elements.get(elementPos).hash) == null)
			{
				NBTac.LOGGER.error("Failed to remove previous cache entry from the map!");
			}
			elements.set(elementPos, newElement);
			elementMap.put(newElement.hash, newElement);
		}
		else if (elementPos == elements.size())
		{
			elements.add(newElement);
			elementMap.put(newElement.hash, newElement);
		}
		else
		{
			NBTac.LOGGER.error("Cache position assigned out of order.");
			clear();
		}
	}

	public int getNextFilePos()
	{
		if (elements.size() < ModConfig.maxCachedInstances.val
				|| ModConfig.maxCachedInstances.val < 0 || elements.isEmpty())
		{
			return elements.size();
		}
		
		Element oldestElement = null;
		long timestamp = Long.MAX_VALUE;
		
		for (Element element : elements)
		{
			if (element.timestamp < timestamp)
			{
				timestamp = element.timestamp;
				oldestElement = element;
			}
		}
		return oldestElement != null ? oldestElement.pos : 0;
	}
	
	public static File getFile(File directory, int elementPos)
	{
		return new File(directory, String.format("%d.txt", elementPos));
	}

	private static class Element
	{
		public final String hash;
		public long timestamp;
		public final int pos;

		public Element(String line, int pos) throws Exception
		{
			String[] components = line.split(" ");
			if (components.length != 2) { throw new Exception("Improper index line component count!"); }
			
			this.hash = components[0];
			this.timestamp = Long.parseLong(components[1]);
			this.pos = pos;
		}

		public Element(String hash, long timestamp, int pos)
		{
			this.hash = hash;
			this.timestamp = timestamp;
			this.pos = pos;
		}

		public String str()
		{
			return String.format("%s %d", hash, timestamp);
		}
	}
}
