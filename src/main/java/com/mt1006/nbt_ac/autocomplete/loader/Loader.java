package com.mt1006.nbt_ac.autocomplete.loader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ParseJson;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ResourceLoader;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.Disassembly;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.TypeLoader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Loader
{
	private static final String SAVE_SUGGESTIONS_FILE = "nbt_ac_output.txt";
	private static final int MAX_PRINTER_DEPTH = 32;
	public static AtomicBoolean finished = new AtomicBoolean(false);

	public static void load()
	{
		long start = System.currentTimeMillis();

		if (ModConfig.useDisassembler.getValue())
		{
			Disassembly.init();
			TypeLoader.loadBlockEntityTypes();
			TypeLoader.loadEntityTypes();
			Disassembly.clear();
		}

		long interruptionStart = System.currentTimeMillis();
		try
		{
			ResourceLoader.countDownLatch.await();
		}
		catch (InterruptedException exception) { NBTac.LOGGER.warn("Unexpected \"ResourceLoader.countDownLatch.await()\" interruption"); }
		long interruptionDuration = System.currentTimeMillis() - interruptionStart;

		if (ModConfig.loadFromResources.getValue())
		{
			ParseJson.parseAll();
		}

		long duration = System.currentTimeMillis() - start;
		NBTac.LOGGER.info("Finished in: " + (duration - interruptionDuration) + " ms [" + duration + " ms with interruption]");
		finished.set(true);

		saveSuggestions(ModConfig.saveSuggestions.getValue());
	}

	public static void saveSuggestions(int mode)
	{
		if (mode == 1 || mode == 2)
		{
			File outputFile = new File(Minecraft.getInstance().gameDirectory, SAVE_SUGGESTIONS_FILE);

			try (PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFile)))
			{
				StringWriter stringWriter = new StringWriter();
				PrintWriter writer = new PrintWriter(stringWriter);

				for (Map.Entry<String, NbtSuggestions> suggestions : NbtSuggestionManager.suggestionMap.entrySet())
				{
					writer.println(suggestions.getKey());
					printSuggestions(writer, suggestions.getKey(), suggestions.getValue(), mode, 1);
					writer.println("");
				}

				if (mode == 2)
				{
					String[] strings = stringWriter.toString().split(System.lineSeparator());
					Arrays.sort(strings);
					for (String str : strings)
					{
						fileWriter.write(str);
					}
				}
				else
				{
					fileWriter.write(stringWriter.toString());
				}

			}
			catch (Exception exception) { NBTac.LOGGER.warn("Failed to save suggestions!"); }
		}
	}

	public static void printSuggestions(PrintWriter writer, String key, NbtSuggestions suggestions, int mode, int depth)
	{
		if (depth > MAX_PRINTER_DEPTH) { return; }

		for (NbtSuggestion suggestion : suggestions.getAll())
		{
			if (mode == 2) { writer.print(key); }
			for (int i = 0; i < depth; i++) { writer.print("-"); }
			writer.printf("%s (%s) [%s/%s] - %s/%s\n", suggestion.tag, suggestion.suggestionType.name,
					suggestion.type.getName(), suggestion.listType.getName(), suggestion.subtype.getName(), suggestion.subtypeData);

			if (suggestion.subcompound != null && suggestions != suggestion.subcompound)
			{
				printSuggestions(writer, key, suggestion.subcompound, mode, depth + 1);
			}
		}
	}
}
