package com.mt1006.nbt_ac.autocomplete.loader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionMap;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Loader
{
	private static final String SAVE_SUGGESTIONS_FILE = "nbt_ac_output.txt";
	private static final int MAX_PRINTER_DEPTH = 32;
	private static final AtomicInteger printedStackTraces = new AtomicInteger();
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

		//TODO: finish
		SuggestionFileParser.parseDataComponents("item", "minecraft");

		NBTac.LOGGER.info("Finished in: {} ms", System.currentTimeMillis() - start);
		finished = true;

		if (ModConfig.debugMode.val)
		{
			NBTac.LOGGER.info("Created NbtSuggestion instances: {}", NbtSuggestion.createdInstanceCounter);
		}

		if (ModConfig.saveSuggestions.val) { saveSuggestions(); }
	}

	private static void saveSuggestions()
	{
		File outputFile = new File(Minecraft.getInstance().gameDirectory, SAVE_SUGGESTIONS_FILE);

		try (PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFile)))
		{
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);

			for (Map.Entry<String, NbtSuggestionMap> suggestions : NbtSuggestionManager.suggestionSet())
			{
				writer.println(suggestions.getKey());
				printSuggestions(writer, suggestions.getValue(), 1);
				writer.println("");
			}

			fileWriter.write(stringWriter.toString());
		}
		catch (Exception e) { NBTac.LOGGER.warn("Failed to save suggestions!"); }
	}

	private static void printSuggestions(PrintWriter writer, @Nullable NbtSuggestionMap suggestions, int depth)
	{
		if (suggestions == null || depth > MAX_PRINTER_DEPTH) { return; }

		for (NbtSuggestion suggestion : suggestions.getAll())
		{
			for (int i = 0; i < depth; i++) { writer.print("-"); }
			writer.printf("%s (%s)\n", suggestion.tag, suggestion.type.getPrimitive().getName());

			if (suggestions != suggestion.subcompound) //TODO: is it still necessary?
			{
				NBTac.LOGGER.error("Loop detected!");
				continue;
			}
			printSuggestions(writer, suggestion.subcompound, depth + 1);
		}
	}

	public static void printStackTrace(Exception exception)
	{
		if (ModConfig.maxStackTraces.val > printedStackTraces.get())
		{
			exception.printStackTrace();
			printedStackTraces.incrementAndGet();
		}
	}
}
