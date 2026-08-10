package com.mt1006.nbt_ac.autocomplete.loader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.cache.TypeCache;
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
import java.util.concurrent.atomic.AtomicInteger;

public class Loader
{
	private static final String SAVE_SUGGESTIONS_FILE = "nbt_ac_output.txt";
	private static final int MAX_PRINTER_DEPTH = 32;
	private static volatile Thread thread;
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
			catch (InterruptedException exception) { NBTac.LOGGER.error("Unexpected debug sleep interruption!"); }
		}

		if (ModConfig.debugMode.val) { NBTac.LOGGER.info("Loader started!"); }
		long start = System.currentTimeMillis();
		thread = Thread.currentThread();

		if (ModConfig.useDisassembler.val)
		{
			boolean cacheEnabled = TypeCache.isEnabled();
			boolean cacheLoaded = cacheEnabled && TypeCache.load();
			if (ModConfig.debugMode.val) { NBTac.LOGGER.info("Cache loaded: {}", cacheLoaded); }

			if (!cacheLoaded)
			{
				Disassembly.init();
				TypeLoader.loadBlockEntityTypes();
				TypeLoader.loadEntityTypes();
				Disassembly.clear();

				if (cacheEnabled) { TypeCache.add(); }
			}
			if (cacheEnabled) { TypeCache.updateIndex(); }
		}

		long interruptionStart = System.currentTimeMillis();
		try
		{
			ResourceLoader.countDownLatch.await();
		}
		catch (InterruptedException exception) { NBTac.LOGGER.error("Unexpected \"ResourceLoader.countDownLatch.await()\" interruption!"); }
		long interruptionDuration = System.currentTimeMillis() - interruptionStart;

		if (ModConfig.loadFromResources.val)
		{
			ParseJson.parseAll();
		}

		long duration = System.currentTimeMillis() - start;
		NBTac.LOGGER.info("Finished in: {} ms [{} ms with interruption]", duration - interruptionDuration, duration);
		finished = true;

		if (ModConfig.debugMode.val)
		{
			NBTac.LOGGER.info("Created NbtSuggestion instances: {}", NbtSuggestion.createdInstanceCounter);
			NBTac.LOGGER.info("Created NbtSuggestions instances: {}", NbtSuggestions.createdInstanceCounter);
		}

		saveSuggestions(SaveSuggestionsMode.get(ModConfig.saveSuggestions.val));
	}

	private static void saveSuggestions(SaveSuggestionsMode mode)
	{
		if (mode.enabled)
		{
			File outputFile = new File(Minecraft.getInstance().gameDirectory, SAVE_SUGGESTIONS_FILE);

			try (PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFile)))
			{
				StringWriter stringWriter = new StringWriter();
				PrintWriter writer = new PrintWriter(stringWriter);

				for (Map.Entry<String, NbtSuggestions> suggestions : NbtSuggestionManager.suggestionSet())
				{
					writer.println(suggestions.getKey());
					printSuggestions(writer, suggestions.getKey(), suggestions.getValue(), mode, 1);
					writer.println("");
				}

				if (mode == SaveSuggestionsMode.ENABLED_SORTED)
				{
					String[] strings = stringWriter.toString().split(System.lineSeparator());
					Arrays.sort(strings);

					for (String str : strings)
					{
						if (str.isEmpty()) { continue; }
						fileWriter.println(str);
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

	private static void printSuggestions(PrintWriter writer, String key, NbtSuggestions suggestions, SaveSuggestionsMode mode, int depth)
	{
		if (depth > MAX_PRINTER_DEPTH) { return; }

		for (NbtSuggestion suggestion : suggestions.getAll())
		{
			if (mode == SaveSuggestionsMode.ENABLED_SORTED) { writer.print(key); }
			for (int i = 0; i < depth; i++) { writer.print("-"); }
			writer.printf("%s (%s) [%s/%s] - %s/%s\n", suggestion.tag, suggestion.source.name,
					suggestion.type.getName(), suggestion.listType.getName(), suggestion.subtype.getName(), suggestion.subtypeData);

			if (suggestion.subcompound != null && suggestions != suggestion.subcompound)
			{
				printSuggestions(writer, key, suggestion.subcompound, mode, depth + 1);
			}
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

	public static Thread getLoaderThread()
	{
		return thread;
	}

	private enum SaveSuggestionsMode
	{
		DISABLED(0, false),
		ENABLED(1, true),
		ENABLED_SORTED(2, true);

		private final int id;
		public final boolean enabled;

		SaveSuggestionsMode(int id, boolean enabled)
		{
			this.id = id;
			this.enabled = enabled;
		}

		public static SaveSuggestionsMode get(int id)
		{
			for (SaveSuggestionsMode mode : values())
			{
				if (mode.id == id) { return mode; }
			}
			return DISABLED;
		}
	}
}
