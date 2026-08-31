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
			new SuggestionDataParser("compound", "nbtac", null).parseNbtSuggestions();
			new SuggestionDataParser("block", "minecraft", null).parseNbtSuggestions();
			new SuggestionDataParser("entity", "minecraft", null).parseNbtSuggestions();
			new SuggestionDataParser("particle", "minecraft", null).parseNbtSuggestions();
			new SuggestionDataParser("text", "nbtac", null).parseNbtSuggestions();
			new SuggestionDataParser("item", "minecraft", null).parseDataComponents();

			new MapDataParser("block2be").parseBlockToBlockEntityMap();
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
