package net.mt1006.nbtac.autocomplete.loader;

import net.minecraft.resources.Identifier;
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
			String val = "block/" + Identifier.parse(entry.header);
			entry.lines.forEach((l) -> NbtTagManager.blockToBlockEntityMap.put(parseLine(l), val));
		}
	}

	public void parseServerRegistries()
	{
		for (Entry entry : parseFile())
		{
			Identifier registryId = Identifier.parse(entry.header);

			List<Identifier> registryEntries = new ArrayList<>();
			entry.lines.forEach((l) -> registryEntries.add(parseLine(l)));
			ServerRegistryKeyType.registryKeyMap.put(registryId, registryEntries);
		}
	}

	private static Identifier parseLine(String line)
	{
		return Identifier.parse(line.substring(1)); // substring(1) - remove '+' sign
	}
}
