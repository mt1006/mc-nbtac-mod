package com.mt1006.nbt_ac.config;

import com.mt1006.nbt_ac.config.gui.ModOptionList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModConfig
{
	private static final ConfigFields fields = new ConfigFields("nbt_ac.txt");


	public static final ConfigFields.BooleanField shortBoolean = fields.add("short_boolean", false);
	public static final ConfigFields.BooleanField tagQuotationMarks = fields.add("tag_quotation_marks", false);
	public static final ConfigFields.BooleanField stringQuotationMarks = fields.add("string_quotation_marks", false);
	private static final ConfigFields.IntegerField defaultQuotationMarkType = fields.add("default_quotation_mark_type", 2);
	private static final ConfigFields.IntegerField jsonStringSuggestion = fields.add("json_string_suggestion", 1);
	public static final ConfigFields.BooleanField hideMcNamespaceInTags = fields.add("hide_mc_namespace_in_tags", true);
	public static final ConfigFields.BooleanField hideMcNamespaceInStrings = fields.add("hide_mc_namespace_in_strings", false);

	public static final ConfigFields.BooleanField ignoreLetterCase = fields.add("ignore_letter_case", true);
	public static final ConfigFields.BooleanField showTagHints = fields.add("show_tag_hints", true);
	public static final ConfigFields.BooleanField hideForgeTags = fields.add("hide_forge_tags", true);

	public static final ConfigFields.BooleanField customSorting = fields.add("custom_sorting", true);
	public static final ConfigFields.BooleanField markRecommended = fields.add("mark_recommended", true);
	public static final ConfigFields.BooleanField recommendedAtTheTop = fields.add("recommended_at_the_top", true);
	public static final ConfigFields.BooleanField markIrrelevant = fields.add("mark_irrelevant", true);
	public static final ConfigFields.BooleanField grayOutIrrelevant = fields.add("gray_out_irrelevant", true);
	public static final ConfigFields.IntegerField placingOfIrrelevant = fields.add("placing_of_irrelevant", 1);

	public static final ConfigFields.BooleanField hideMcNamespaceInComponents = fields.add("hide_mc_namespace_in_components", true);
	public static final ConfigFields.BooleanField showCustomDataAsRelevant = fields.add("show_custom_data_as_relevant", false);
	public static final ConfigFields.BooleanField showCustomModelDataAsRelevant = fields.add("show_custom_model_data_as_relevant", false);

	//public static final ConfigFields.IntegerField vanillaIdsSorting = fields.add("vanilla_ids_sorting", 1); //TODO: implement
	public static final ConfigFields.BooleanField supportCommandNamespace = fields.add("support_command_namespace", true);

	public static final ConfigFields.BooleanField useNewThread = fields.add("use_new_thread", true);
	public static final ConfigFields.BooleanField useDisassembler = fields.add("use_disassembler", true);
	public static final ConfigFields.BooleanField loadFromResources = fields.add("load_from_resources", true);
	public static final ConfigFields.BooleanField allowBlockEntityExtraction = fields.add("allow_block_entity_extraction", true);
	public static final ConfigFields.BooleanField useCache = fields.add("use_cache", true);
	public static final ConfigFields.IntegerField maxCachedInstances = fields.add("max_cached_instances", 32);

	public static final ConfigFields.IntegerField maxStackTraces = fields.add("max_stack_traces", 6);
	public static final ConfigFields.BooleanField debugMode = fields.add("debug_mode", false);
	public static final ConfigFields.IntegerField debugSleep = fields.add("debug_sleep", 0);
	public static final ConfigFields.IntegerField saveSuggestions = fields.add("save_suggestions", 0);
	public static final ConfigFields.BooleanField debugConfigScreen = fields.add("debug_config_screen", false);


	public static void initWidgets(ModOptionList list)
	{
		if (debugConfigScreen.val) { list.addLabel("common.gui_debug_warning"); }

		list.addLabel("style");
		list.add(shortBoolean.createSwitch());
		list.add(tagQuotationMarks.createDescribedSwitch());
		list.add(stringQuotationMarks.createDescribedSwitch());
		list.add(defaultQuotationMarkType.createSwitch(List.of(1, 2)));
		list.add(jsonStringSuggestion.createSwitch(List.of(0, 1, 2, 3, 4, 5)));
		list.add(hideMcNamespaceInTags.createSwitch());
		list.add(hideMcNamespaceInStrings.createSwitch());

		list.addLabel("suggestions");
		list.add(ignoreLetterCase.createSwitch());
		list.add(showTagHints.createSwitch());
		list.add(hideForgeTags.createSwitch());

		list.addLabel("suggestion_priority");
		list.add(customSorting.createSwitch());
		list.add(markRecommended.createSwitch());
		list.add(recommendedAtTheTop.createSwitch());
		list.add(markIrrelevant.createSwitch());
		list.add(grayOutIrrelevant.createSwitch());
		list.add(placingOfIrrelevant.createSwitch(List.of(0, 1, 2)));

		list.addLabel("mods_and_plugins_support");
		//list.add(vanillaIdsSorting.createSwitch(List.of(0, 1, 2, 3)));
		list.add(supportCommandNamespace.createSwitch());

		list.addLabel("advanced_settings");
		list.add(useNewThread.createSwitch());
		list.add(useDisassembler.createSwitch());
		list.add(loadFromResources.createSwitch());
		list.add(allowBlockEntityExtraction.createSwitch());
		list.add(useCache.createSwitch());
		list.add(maxCachedInstances.createSlider(-1, 64, 1, List.of(-1)));

		list.addLabel("debugging_options");
		list.add(maxStackTraces.createSlider(-1, 96, 1, List.of(-1, 0)));
		list.add(debugMode.createSwitch());
		list.add(debugSleep.createSlider(0, 100, 200, List.of(0)));
		list.add(saveSuggestions.createSwitch(List.of(0, 1, 2)));
	}

	public static void load()
	{
		fields.load();
	}

	public static void save()
	{
		fields.save();
	}

	public static void reset()
	{
		boolean debugVal = debugConfigScreen.val;
		fields.reset();
		debugConfigScreen.val = debugVal;
	}

	public static char getDefaultQuotationMark(boolean isRawJson)
	{
		return (defaultQuotationMarkType.val == 1 && !isRawJson) ? '\'' : '"';
	}

	public static @Nullable String getJsonStringSuggestion()
	{
		return switch (jsonStringSuggestion.val)
		{
			case 0 -> null;
			default -> String.valueOf(getDefaultQuotationMark(false));
			case 3 -> "' \"";
			case 4 -> "'\"";
			case 5 -> "\"\\\"";
		};
	}
}
