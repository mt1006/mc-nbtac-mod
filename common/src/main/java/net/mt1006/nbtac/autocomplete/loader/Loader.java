package net.mt1006.nbtac.autocomplete.loader;

import net.minecraft.client.Minecraft;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.tag.DefinedNbtTag;
import net.mt1006.nbtac.autocomplete.tag.NbtTag;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class Loader
{
	private static final String SAVE_SUGGESTIONS_FILE = "nbtac_output.txt";
	private static final int MAX_PRINTER_DEPTH = 32;
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
			SuggestionFileParser.parseDataComponents("item", "minecraft");
			SuggestionFileParser.parseNbtSuggestions("block", "minecraft");
			SuggestionFileParser.parseNbtSuggestions("entity", "minecraft");
			SuggestionFileParser.parseNbtSuggestions("text", "nbtac");
			SuggestionFileParser.parseNbtSuggestions("compound", "nbtac");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		NBTac.LOGGER.info("Finished in: {} ms", System.currentTimeMillis() - start);
		finished = true;

		if (ModConfig.debugMode.val)
		{
			NBTac.LOGGER.info("Created NbtSuggestion instances: {}", DefinedNbtTag.instanceCounter);
		}

		if (ModConfig.saveSuggestions.val) { saveTags(); }
	}

	private static void saveTags()
	{
		File outputFile = new File(Minecraft.getInstance().gameDirectory, SAVE_SUGGESTIONS_FILE);

		try (PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFile)))
		{
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);

			for (Map.Entry<String, NbtTagMap> suggestions : NbtTagManager.tagMapSet())
			{
				writer.println(suggestions.getKey());
				printTags(writer, suggestions.getValue(), 1);
				writer.println("");
			}

			fileWriter.write(stringWriter.toString());
		}
		catch (Exception e) { NBTac.LOGGER.warn("Failed to save suggestions!"); }
	}

	private static void printTags(PrintWriter writer, @Nullable NbtTagMap tagMap, int depth)
	{
		if (tagMap == null || depth > MAX_PRINTER_DEPTH) { return; }

		for (NbtTag tag : tagMap.getAll())
		{
			for (int i = 0; i < depth; i++) { writer.print("-"); }
			writer.printf("%s (%s)\n", tag.getName(), tag.getType().getPrimitive().getName());

			try { printTags(writer, tag.getType().getSubcompound(), depth + 1); }
			catch (UnsupportedOperationException ignore) {}
		}
	}
}
