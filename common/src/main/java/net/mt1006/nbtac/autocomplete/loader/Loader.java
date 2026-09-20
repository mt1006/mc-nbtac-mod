package net.mt1006.nbtac.autocomplete.loader;

import net.minecraft.client.Minecraft;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.DataSource;
import net.mt1006.nbtac.config.ModConfig;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Loader
{
	public static volatile boolean finished = false;

	public static void load()
	{
		ModConfig.load();

		int debugSleep = ModConfig.debugSleep.val;
		if (debugSleep > 0)
		{
			NBTac.LOGGER.info("Debug sleep enabled! - Sleeping: {} ms", debugSleep);
			try { Thread.sleep(debugSleep); }
			catch (InterruptedException e) { NBTac.LOGGER.error("Unexpected debug sleep interruption!"); }
		}

		if (ModConfig.debugMode.val) { NBTac.LOGGER.info("Loader started!"); }
		long start = System.currentTimeMillis();

		try
		{
			new SuggestionDataParser("compound", "nbtac", null).parseNbtSuggestions(DataSource.BUILTIN);
			new SuggestionDataParser("block", "minecraft", null).parseNbtSuggestions(DataSource.BUILTIN);
			new SuggestionDataParser("entity", "minecraft", null).parseNbtSuggestions(DataSource.BUILTIN);
			new SuggestionDataParser("particle", "minecraft", null).parseNbtSuggestions(DataSource.BUILTIN);
			new SuggestionDataParser("text", "nbtac", null).parseNbtSuggestions(DataSource.BUILTIN);
			new SuggestionDataParser("item", "minecraft", null).parseDataComponents(DataSource.BUILTIN);

			new MapDataParser("block2be", true).parseBlockToBlockEntityMap();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		loadCustomSuggestions();

		finished = true;
		if (ModConfig.debugMode.val)
		{
			NBTac.LOGGER.info("Finished in: {} ms", System.currentTimeMillis() - start);
		}
	}

	private static void loadCustomSuggestions()
	{
		File suggestionsDir = new File(Minecraft.getInstance().gameDirectory, "config/nbtac_suggestions");
		File[] dirFiles = suggestionsDir.listFiles();
		if (dirFiles == null) { return; }

		for (File f : dirFiles)
		{
			String[] nameParts = f.getName().split("\\.");
			if (!f.isFile() || nameParts.length != 3)
			{
				NBTac.LOGGER.warn("Failed to load suggestions file {} - invalid type or name!", f.getName());
				continue;
			}

			try
			{
				if (nameParts[2].equals("b2be"))
				{
					// nameParts[0] and nameParts[1] are ignored
					MapDataParser parser = new MapDataParser(Files.readString(f.toPath(), StandardCharsets.UTF_8), false);
					parser.parseBlockToBlockEntityMap();
				}
				else
				{
					SuggestionDataParser parser = new SuggestionDataParser(nameParts[1], nameParts[0],
							Files.readString(f.toPath(), StandardCharsets.UTF_8));
					if (nameParts[2].equals("nbts")) { parser.parseNbtSuggestions(DataSource.USER_DEFINED); }
					else if (nameParts[2].equals("comp")) { parser.parseDataComponents(DataSource.USER_DEFINED); }
					else { throw new RuntimeException("Invalid suggestion file suffix: " + nameParts[2]); }
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				NBTac.LOGGER.warn("Failed to load suggestion file {}!", f.getName());
			}
		}
	}
}
