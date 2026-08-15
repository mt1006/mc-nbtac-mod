package net.mt1006.nbtac.autocomplete.loader;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.type.complex.ServerRegistryKeyType;

import java.util.ArrayList;
import java.util.List;

public class DataFileParser extends FileParser
{
	protected DataFileParser(String filename)
	{
		super(filename);
	}

	public void parseBlockToBlockEntityMap()
	{
		for (Entry entry : parseFile())
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
