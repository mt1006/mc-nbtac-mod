package com.mt1006.nbt_ac.config;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.File;

public class ModConfig
{
	private static final String CONFIG_FILE_NAME = "config/nbt_ac.txt";
	private static ConfigFile configFile;

	public static MutableBoolean useDisassembler = new MutableBoolean(true);
	public static MutableBoolean loadFromResources = new MutableBoolean(true);
	public static MutableBoolean useNewThread = new MutableBoolean(true);
	public static MutableBoolean shortBoolean = new MutableBoolean(false);
	public static MutableBoolean hideForgeTags = new MutableBoolean(true);
	public static MutableBoolean showTagTypes = new MutableBoolean(true);
	public static MutableBoolean predictSuggestions = new MutableBoolean(true);

	public static void initConfig()
	{
		configFile = new ConfigFile(new File(Minecraft.getInstance().gameDirectory, CONFIG_FILE_NAME));

		configFile.addValue("use_disassembler", useDisassembler,
				"Load suggestions by using disassembler on \"load\" methods of entities and block entities.");

		configFile.addValue("load_from_resources", loadFromResources,
				"Load suggestions from resource files (required to load item nbt suggestions).");

		configFile.addValue("use_new_thread", useNewThread,
				"Use new thread to load suggestions after loading Minecraft.");

		configFile.addValue("short_boolean", shortBoolean,
				"Suggest 1b/0b instead of true/false for boolean value.");

		configFile.addValue("hide_forge_tags", hideForgeTags,
				"Hide \"ForgeCaps\" and \"ForgeData\" tags.");

		configFile.addValue("show_tag_types", showTagTypes,
				"Show tag type next to its name in suggestions list.");

		configFile.addValue("predict_suggestions", predictSuggestions,
				"Predict suggestions for known tags.");
	}

	public static void loadConfig()
	{
		configFile.load();
	}
}
