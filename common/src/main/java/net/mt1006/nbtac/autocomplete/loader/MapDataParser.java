package net.mt1006.nbtac.autocomplete.loader;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.NbtTagManager;

public class MapDataParser extends FileParser
{
	protected MapDataParser(String filename)
	{
		super(filename, true);
	}

	public void parseBlockToBlockEntityMap()
	{
		for (Entry entry : parseLines())
		{
			String val = "block/" + Identifier.parse(entry.header);
			entry.lines.forEach((l) -> NbtTagManager.blockToBlockEntityMap.put(parseLine(l), val));
		}
	}

	private static Identifier parseLine(String line)
	{
		return Identifier.parse(line.substring(1)); // substring(1) - remove '+' sign
	}
}
