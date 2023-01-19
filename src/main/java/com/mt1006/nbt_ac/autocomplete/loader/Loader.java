package com.mt1006.nbt_ac.autocomplete.loader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.resourceloader.ParseJson;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.Disassembly;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.TypeLoader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Loader
{
	public static final boolean SAVE_SUGGESTIONS = false;
	public static AtomicBoolean finished = new AtomicBoolean(false);

	public static void load()
	{
		long start = System.currentTimeMillis();

		if (ModConfig.useDisassembler.getValue())
		{
			Disassembly.init();
			TypeLoader.loadBlockEntityTypes();
			TypeLoader.loadEntityTypes();
		}

		if (ModConfig.loadFromResources.getValue())
		{
			ParseJson.parseAll();
		}

		NBTac.LOGGER.info("Finished in: " + (int)(System.currentTimeMillis() - start) + " ms");
		finished.set(true);

		if (SAVE_SUGGESTIONS)
		{
			File outputFile = new File(Minecraft.getInstance().gameDirectory, "nbt_ac_output.txt");

			try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile)))
			{
				for (Map.Entry<String, NbtSuggestions> suggestions : NbtSuggestionManager.getSuggestionMap().entrySet())
				{
					writer.println(suggestions.getKey());
					printSuggestions(writer, null, suggestions.getValue(), 1);
					writer.println("");
				}
			}
			catch (Exception exception) { NBTac.LOGGER.warn("Failed to save suggestions!"); }
		}
	}

	public static void printSuggestions(PrintWriter writer, NbtSuggestion parent, NbtSuggestions suggestions, int depth)
	{
		for (NbtSuggestion suggestion : suggestions.suggestions)
		{
			for (int i = 0; i < depth; i++) { writer.print("-"); }
			writer.printf("%s (%s) [%s/%s] - %s/%s\n", suggestion.tag, suggestion.suggestionType.name,
					suggestion.type.getName(), suggestion.listType.getName(), suggestion.subtype.getName(), suggestion.subtypeData);

			// Debug:
			if (parent != null && suggestion.tag.equalsIgnoreCase("id") &&
					!parent.tag.endsWith("Items") && !parent.tag.endsWith("Item") && !parent.tag.endsWith("Effects"))
			{
				NBTac.LOGGER.info("ID parent: " + parent.tag);
			}

			if (suggestion.subcompound != null) { printSuggestions(writer, suggestion, suggestion.subcompound, depth + 1); }
		}
	}
}
