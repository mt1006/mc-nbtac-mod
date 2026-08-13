package net.mt1006.nbtac.config;

import net.mt1006.nbtac.config.enums.DefaultQuotationMark;
import net.mt1006.nbtac.config.enums.JsonStringSuggestion;
import net.mt1006.nbtac.config.enums.PlacingOfIrrelevant;
import net.mt1006.nbtac.config.enums.UnknownItemComponents;
import net.mt1006.nbtac.config.gui.ModOptionList;

import java.util.List;

public class ModConfig
{
	private static final ConfigFields fields = new ConfigFields("nbtac.txt");


	public static final ConfigFields.BooleanField shortBoolean = fields.add("short_boolean", false);
	public static final ConfigFields.BooleanField tagQuotationMarks = fields.add("tag_quotation_marks", false);
	public static final ConfigFields.BooleanField stringQuotationMarks = fields.add("string_quotation_marks", false);
	public static final ConfigFields.EnumField<DefaultQuotationMark> defaultQuotationMark = fields.add("default_quotation_mark", DefaultQuotationMark.DOUBLE);
	public static final ConfigFields.EnumField<JsonStringSuggestion> jsonStringSuggestion = fields.add("json_string_suggestion", JsonStringSuggestion.DEFAULT_FOR_STRINGS);
	public static final ConfigFields.BooleanField hideMcNamespaceInTags = fields.add("hide_mc_namespace_in_tags", true);
	public static final ConfigFields.BooleanField hideMcNamespaceInStrings = fields.add("hide_mc_namespace_in_strings", false);

	public static final ConfigFields.BooleanField ignoreLetterCase = fields.add("ignore_letter_case", true);
	public static final ConfigFields.BooleanField showTagHints = fields.add("show_tag_hints", true);

	public static final ConfigFields.BooleanField customSorting = fields.add("custom_sorting", true);
	public static final ConfigFields.BooleanField markRecommended = fields.add("mark_recommended", true);
	public static final ConfigFields.BooleanField recommendedAtTheTop = fields.add("recommended_at_the_top", true);
	public static final ConfigFields.BooleanField markIrrelevant = fields.add("mark_irrelevant", true);
	public static final ConfigFields.BooleanField grayOutIrrelevant = fields.add("gray_out_irrelevant", true);
	public static final ConfigFields.EnumField<PlacingOfIrrelevant> placingOfIrrelevant = fields.add("placing_of_irrelevant", PlacingOfIrrelevant.AT_THE_BOTTOM);

	public static final ConfigFields.BooleanField hideMcNamespaceInComponents = fields.add("hide_mc_namespace_in_components", true);
	public static final ConfigFields.BooleanField showCustomDataAsRelevant = fields.add("show_custom_data_as_relevant", false);
	public static final ConfigFields.BooleanField showCustomModelDataAsRelevant = fields.add("show_custom_model_data_as_relevant", false);

	//public static final ConfigFields.IntegerField vanillaIdsSorting = fields.add("vanilla_ids_sorting", 1); //TODO: implement
	public static final ConfigFields.EnumField<UnknownItemComponents> unknownItemComponents = fields.add("unknown_item_components", UnknownItemComponents.RELEVANT_BY_DEFAULT);
	public static final ConfigFields.BooleanField supportCommandNamespace = fields.add("support_command_namespace", true);

	public static final ConfigFields.BooleanField useNewThread = fields.add("use_new_thread", true);

	public static final ConfigFields.BooleanField debugMode = fields.add("debug_mode", false);
	public static final ConfigFields.IntegerField debugSleep = fields.add("debug_sleep", 0);
	public static final ConfigFields.BooleanField debugConfigScreen = fields.add("debug_config_screen", false);


	public static void initWidgets(ModOptionList list)
	{
		if (debugConfigScreen.val) { list.addLabel("common.gui_debug_warning"); }

		list.addLabel("style");
		list.add(shortBoolean.createSwitch());
		list.add(tagQuotationMarks.createDescribedSwitch());
		list.add(stringQuotationMarks.createDescribedSwitch());
		list.add(defaultQuotationMark.createSwitch());
		list.add(jsonStringSuggestion.createSwitch());
		list.add(hideMcNamespaceInTags.createSwitch());
		list.add(hideMcNamespaceInStrings.createSwitch());

		list.addLabel("suggestions");
		list.add(ignoreLetterCase.createSwitch());
		list.add(showTagHints.createSwitch());

		list.addLabel("suggestion_priority");
		list.add(customSorting.createSwitch());
		list.add(markRecommended.createSwitch());
		list.add(recommendedAtTheTop.createSwitch());
		list.add(markIrrelevant.createSwitch());
		list.add(grayOutIrrelevant.createSwitch());
		list.add(placingOfIrrelevant.createSwitch());

		list.addLabel("item_components");
		list.add(hideMcNamespaceInComponents.createSwitch());
		list.add(showCustomDataAsRelevant.createSwitch());
		list.add(showCustomModelDataAsRelevant.createSwitch());

		list.addLabel("mods_and_plugins_support");
		//list.add(vanillaIdsSorting.createSwitch(List.of(0, 1, 2, 3)));
		list.add(unknownItemComponents.createSwitch());
		list.add(supportCommandNamespace.createSwitch());

		list.addLabel("advanced_settings");
		list.add(useNewThread.createSwitch());

		list.addLabel("debugging_options");
		list.add(debugMode.createSwitch());
		list.add(debugSleep.createSlider(0, 100, 200, List.of(0)));
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
}
