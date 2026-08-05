package net.mt1006.nbtac.autocomplete.loader;

import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.config.ModConfig;

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
			new SuggestionFileParser("compound", "nbtac").parseNbtSuggestions();
			new SuggestionFileParser("block", "minecraft").parseNbtSuggestions();
			new SuggestionFileParser("entity", "minecraft").parseNbtSuggestions();
			new SuggestionFileParser("text", "nbtac").parseNbtSuggestions();
			new SuggestionFileParser("item", "minecraft").parseDataComponents();

			new DataFileParser("block2be").parseBlockToBlockEntityMap();
			new DataFileParser("serverreg").parseServerRegistries();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		finished = true;
		if (ModConfig.debugMode.val)
		{
			NBTac.LOGGER.info("Finished in: {} ms", System.currentTimeMillis() - start);
		}
	}
}
