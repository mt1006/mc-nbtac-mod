package net.mt1006.nbtac.autocomplete.loader;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.NbtTagManager;

public class MapDataParser extends DataParser
{
	public MapDataParser(String filename, boolean dataAsFilename)
	{
		super(filename, dataAsFilename);
	}

	public void parseBlockToBlockEntityMap()
	{
		for (Entry entry : parseLines())
		{
			String val = "block/" + ResourceLocation.tryParse(entry.header);
			entry.lines.forEach((l) -> NbtTagManager.blockToBlockEntityMap.put(parseLine(l), val));
		}
	}

	private static ResourceLocation parseLine(String line)
	{
		return ResourceLocation.tryParse(line.substring(1)); // substring(1) - remove '+' sign
	}
}
