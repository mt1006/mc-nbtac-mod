package com.mt1006.nbt_ac.config;

import com.google.common.collect.ImmutableList;
import com.mt1006.nbt_ac.config.gui.ModOptionList;

public class ModConfig
{
	private static final ConfigFields configFields = new ConfigFields("nbt_ac.txt");

	public static final ConfigFields.BooleanField ignoreLetterCase = configFields.add("ignore_letter_case", true);
	public static final ConfigFields.BooleanField showTagTypes = configFields.add("show_tag_types", true);
	public static final ConfigFields.BooleanField shortBoolean = configFields.add("short_boolean", false);
	public static final ConfigFields.BooleanField hideForgeTags = configFields.add("hide_forge_tags", true);
	public static final ConfigFields.BooleanField supportCommandNamespace = configFields.add("support_command_namespace", true);
	public static final ConfigFields.BooleanField useNewThread = configFields.add("use_new_thread", true);
	public static final ConfigFields.BooleanField useDisassembler = configFields.add("use_disassembler", true);
	public static final ConfigFields.BooleanField loadFromResources = configFields.add("load_from_resources", true);
	public static final ConfigFields.BooleanField useCache = configFields.add("use_cache", true);
	public static final ConfigFields.IntegerField maxCachedInstances = configFields.add("max_cached_instances", 32);
	public static final ConfigFields.IntegerField maxStackTraces = configFields.add("max_stack_traces", 6);
	public static final ConfigFields.BooleanField debugMode = configFields.add("debug_mode", false);
	public static final ConfigFields.IntegerField debugSleep = configFields.add("debug_sleep", 0);
	public static final ConfigFields.IntegerField saveSuggestions = configFields.add("save_suggestions", 0);
	public static final ConfigFields.BooleanField debugConfigScreen = configFields.add("debug_config_screen", false);

	public static void initWidgets(ModOptionList list)
	{
		if (debugConfigScreen.val) { list.addLabel("common.gui_debug_warning.1"); }

		list.addLabel("customization");
		list.add(ignoreLetterCase.createSwitch());
		list.add(showTagTypes.createSwitch());
		list.add(shortBoolean.createSwitch());
		list.add(hideForgeTags.createSwitch());

		list.addLabel("mods_and_plugins_support");
		list.add(supportCommandNamespace.createSwitch());

		list.addLabel("advanced_settings");
		list.add(useNewThread.createSwitch());
		list.add(useDisassembler.createSwitch());
		list.add(loadFromResources.createSwitch());
		list.add(useCache.createSwitch());
		list.add(maxCachedInstances.createSlider(-1, 64, 1, ImmutableList.of(-1)));

		list.addLabel("debugging_options");
		list.add(maxStackTraces.createSlider(-1, 96, 1, ImmutableList.of(-1, 0)));
		list.add(debugMode.createSwitch());
		list.add(debugSleep.createSlider(0, 100, 200, ImmutableList.of(0)));
		list.add(saveSuggestions.createSwitch(ImmutableList.of(0, 1, 2)));
	}

	public static void load()
	{
		configFields.load();
	}

	public static void save()
	{
		configFields.save();
	}

	public static void reset()
	{
		boolean debugVal = debugConfigScreen.val;
		configFields.reset();
		debugConfigScreen.val = debugVal;
	}
}
