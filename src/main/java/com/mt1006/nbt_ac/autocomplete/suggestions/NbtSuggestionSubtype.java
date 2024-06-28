package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.SuggestionList;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.mixin.fields.FontManagerFields;
import com.mt1006.nbt_ac.mixin.fields.KeyMappingFields;
import com.mt1006.nbt_ac.mixin.fields.MinecraftFields;
import com.mt1006.nbt_ac.mixin.fields.TextColorFields;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum NbtSuggestionSubtype
{
	//TODO: improve json text scoreboard value suggestions
	//TODO: improve "type":"nbt" suggestions in json text
	NONE,                 // subtypeData = null
	ENUM,                 // subtypeData = "1;2;aaa"
	DESCRIBED_ENUM,       // subtypeData = "1;description;2;description;3;;4"
	BLOCK_STATE_ENUM,     // subtypeData = "up;down"
	TAG,                  // subtypeData = "item/minecraft:compass"
	BLOCK_STATE_TAG,      // subtypeData = "minecraft:chest" (can also start with "block/" or "item/")
	SPAWN_EGG,            // subtypeData = "minecraft:allay_spawn_egg" (can also start with "item/")
	REGISTRY_ID,          // subtypeData = "minecraft:mob_effect"
	REGISTRY_KEY,         // subtypeData = "minecraft:mob_effect"
	RECIPE,               // subtypeData = null
	FONT,                 // subtypeData = null
	RANDOM_UUID,          // subtypeData = null
	INVENTORY_SLOT,       // subtypeData = null
	KEYBIND,              // subtypeData = null //TODO: do something about suggestion list going out of the screen
	JSON_TEXT,            // subtypeData = null
	JSON_TEXT_COMPOUND,   // subtypeData = null
	JSON_TEXT_COLOR,      // subtypeData = null
	ENTITY_SELECTOR,      // subtypeData = null
	HOVER_EVENT_CONTENTS, // subtypeData = null //TODO: replace with more generic subtype (and improve suggestions for show_text)
	ENCHANTMENTS;         // subtypeData = null

	private static final List<String> JSON_TEXT_TYPE_SPECIFIC = List.of("nbt", "translatable");
	private static final List<String> JSON_TEXT_TYPE_HAS_SEPARATOR = List.of("nbt", "selector");

	public String getName()
	{
		return name().toLowerCase();
	}

	public static NbtSuggestionSubtype fromName(String name)
	{
		try
		{
			return valueOf(name.toUpperCase());
		}
		catch (IllegalArgumentException exception) { return NONE; }
	}

	public <T> boolean getSubtypeSuggestions(NbtSuggestion suggestion, SuggestionList suggestionList,
											 @Nullable String data, CustomTagParser.Type parserType)
	{
		switch (this)
		{
			case ENUM:
				if (data == null) { break; }
				suggestionList.clear();

				for (String substring : data.split(";"))
				{
					suggestionList.add(CustomSuggestion.fromType(substring, null, suggestion.type, parserType, 0));
				}
				return true;

			case DESCRIBED_ENUM:
				if (data == null) { break; }
				suggestionList.clear();

				String suggestionText = null;
				for (String substring : data.split(";"))
				{
					if (suggestionText == null)
					{
						suggestionText = substring;
					}
					else
					{
						suggestionList.add(CustomSuggestion.fromType(suggestionText,
								String.format("<%s>", substring), suggestion.type, parserType, 0));
						suggestionText = null;
					}
				}
				return true;

			case REGISTRY_KEY:
			case REGISTRY_ID:
				if (data == null) { break; }

				try
				{
					ResourceLocation registryLocation = new ResourceLocation(data);
					Registry<T> registry = (Registry<T>)RegistryUtils.REGISTRY.get(registryLocation);
					if (registry == null) { break; }

					suggestionList.clear();
					if (this == REGISTRY_ID)
					{
						//TODO: remove it in the future, when it's no longer necessary
						for (T object : registry)
						{
							suggestionList.addRaw(Integer.toString(registry.getId(object)),
									"\"" + registry.getKey(object) + "\" [#" + registryLocation.getPath() + "]");
						}
					}
					else
					{
						for (T object : registry)
						{
							suggestionList.add(new IdSuggestion(registry.getKey(object),
									"[#" + registryLocation.getPath() + "]", parserType));
						}
					}
				}
				catch (Exception ignore) {}
				return true;

			case RECIPE:
				ClientLevel recipeLevel = Minecraft.getInstance().level;
				if (recipeLevel == null) { break; }

				suggestionList.clear();
				for (ResourceLocation id : recipeLevel.getRecipeManager().getRecipeIds().toArray(ResourceLocation[]::new))
				{
					suggestionList.add(new IdSuggestion(id, null, parserType));
				}
				return true;

			case FONT:
				suggestionList.clear();
				FontManager fontManager = ((MinecraftFields)Minecraft.getInstance()).getFontManager();
				for (ResourceLocation id : ((FontManagerFields)fontManager).getFontSets().keySet())
				{
					suggestionList.add(new IdSuggestion(id, "[#font]", parserType));
				}
				return true;

			case JSON_TEXT:
				suggestionList.clear();
				getJsonTextPrefixSuggestions(suggestionList, true);
				return true;

			case JSON_TEXT_COLOR:
				suggestionList.clear();
				suggestionList.addRaw("\"#", "[#json_color]", 1);

				Map<String, TextColor> colorMap = TextColorFields.getNAMED_COLORS();
				if (colorMap == null) { return true; }
				for (Map.Entry<String, TextColor> entry : colorMap.entrySet())
				{
					String subtext = String.format("(#%06X) [#json_color]", entry.getValue().getValue());
					suggestionList.add(new StringSuggestion(entry.getKey(), subtext, parserType));
				}
				return true;

			case RANDOM_UUID:
				suggestionList.clear();

				UUID randomUUID = UUID.randomUUID();
				int uuidInt0 = (int)randomUUID.getLeastSignificantBits();
				int uuidInt1 = (int)(randomUUID.getLeastSignificantBits() >>> 32);
				int uuidInt2 = (int)randomUUID.getMostSignificantBits();
				int uuidInt3 = (int)(randomUUID.getMostSignificantBits() >>> 32);

				//TODO: add setting to remove spaces?
				String uuidString = String.format("[I;%d, %d, %d, %d]", uuidInt3, uuidInt2, uuidInt1, uuidInt0);
				suggestionList.addRaw(uuidString, "[#random_uuid]");
				return true;

			case INVENTORY_SLOT:
				suggestionList.clear();
				NbtSuggestion.Type type = suggestion.type;

				for (int i = 0; i < 9; i++)
				{
					String subtext = String.format("(Hotbar %d) [#inventory_slot]", i + 1);
					suggestionList.addRaw(String.format("%d%s", i, type.suffix), subtext);
				}

				for (int i = 9; i < 35; i++)
				{
					int row = ((i - 9) / 9) + 1;
					int column = ((i - 9) % 9) + 1;
					String subtext = String.format("(Storage %d:%d) [#inventory_slot]", row, column);
					suggestionList.addRaw(String.format("%d%s", i, type.suffix), subtext);
				}

				suggestionList.addRaw("100" + type.suffix, "(Feet) [#inventory_slot]");
				suggestionList.addRaw("101" + type.suffix, "(Legs) [#inventory_slot]");
				suggestionList.addRaw("102" + type.suffix, "(Chest) [#inventory_slot]");
				suggestionList.addRaw("103" + type.suffix, "(Head) [#inventory_slot]");
				suggestionList.addRaw("-106" + type.suffix, "(Off-hand) [#inventory_slot]");
				return true;

			case KEYBIND:
				suggestionList.clear();
				Map<String, KeyMapping> keyMap = KeyMappingFields.getALL();
				if (keyMap == null) { break; }

				for (String str : keyMap.keySet())
				{
					String subtext = "\"" + Component.translatable(str).getString() + "\" [#keybind]";
					suggestionList.add(new StringSuggestion(str, subtext, parserType));
				}
				return true;

			case ENTITY_SELECTOR:
				//TODO: improve it using EntitySelectorParser
				suggestionList.clear();
				suggestionList.add(new StringSuggestion("@p", "[#entity_selector]", parserType));
				suggestionList.add(new StringSuggestion("@a", "[#entity_selector]", parserType));
				suggestionList.add(new StringSuggestion("@r", "[#entity_selector]", parserType));
				suggestionList.add(new StringSuggestion("@s", "[#entity_selector]", parserType));
				suggestionList.add(new StringSuggestion("@e", "[#entity_selector]", parserType));
				return true;
		}

		return false;
	}

	public <T extends Comparable<T>> void getSubtypeTagSuggestions(SuggestionList suggestionList, NbtSuggestion.ParentInfo parentInfo,
																   @Nullable String data, CustomTagParser.Type parserType)
	{
		switch (this)
		{
			case TAG:
				if (data == null) { break; }
				data = data.replace("block/item/", "block/");
				data = data.replace("entity/item/", "entity/");

				NbtSuggestions tagSuggestions = NbtSuggestionManager.get(data);
				suggestionList.addAll(tagSuggestions, data, parserType);
				break;

			case BLOCK_STATE_TAG:
				try
				{
					if (data == null) { break; }

					if (data.startsWith("block/")) { data = data.substring(6); }
					else if (data.startsWith("item/")) { data = data.substring(5); }

					Item blockItem = RegistryUtils.ITEM.get(new ResourceLocation(data));
					if (!(blockItem instanceof BlockItem)) { break; }

					for (Property<?> property : ((BlockItem)blockItem).getBlock().defaultBlockState().getProperties())
					{
						NbtSuggestion nbtSuggestion = new NbtSuggestion(property.getName(), NbtSuggestion.Type.STRING);
						nbtSuggestion.subtype = NbtSuggestionSubtype.BLOCK_STATE_ENUM;

						StringBuilder enumStringBuilder = new StringBuilder();
						for (T possibleValue : ((Property<T>)property).getPossibleValues())
						{
							enumStringBuilder.append(((Property<T>)property).getName(possibleValue)).append(";");
						}
						nbtSuggestion.subtypeData = enumStringBuilder.toString();

						suggestionList.add(new TagSuggestion(nbtSuggestion, parserType));
					}
				}
				catch (Exception ignore) {}
				break;

			case SPAWN_EGG:
				try
				{
					if (data == null) { break; }
					if (data.startsWith("item/")) { data = data.substring(5); }

					Item item = RegistryUtils.ITEM.get(new ResourceLocation(data));
					if (item instanceof SpawnEggItem)
					{
						String key = RegistryUtils.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(null)).toString();
						NbtSuggestions spawnEggSuggestions = NbtSuggestionManager.get("entity/" + key);
						suggestionList.addAll(spawnEggSuggestions, data, parserType);
					}
				}
				catch (Exception ignore) {}
				break;

			case JSON_TEXT_COMPOUND:
				getJsonCompoundSuggestions(suggestionList, parentInfo, parserType);
				break;

			case HOVER_EVENT_CONTENTS:
				getHoverEventContentsSuggestions(suggestionList, parentInfo, data, parserType);
				break;

			case ENCHANTMENTS:
				for (ResourceLocation id : RegistryUtils.ENCHANTMENT.keySet())
				{
					NbtSuggestion tempSuggestion = new NbtSuggestion(id.toString(), NbtSuggestion.Type.INT);
					suggestionList.add(new TagIdSuggestion(tempSuggestion, id, parserType, true));
				}
				break;
		}
	}

	//TODO: move json-related methods to a new class
	public static void getJsonTextPrefixSuggestions(SuggestionList suggestionList, boolean inner)
	{
		String jsonSuggestion = inner ? ModConfig.getJsonStringSuggestion() : "\"";
		if (jsonSuggestion != null) { suggestionList.addRaw(jsonSuggestion, "(simple string) [#json_text]", 3); }
		suggestionList.addRaw(inner ? "'{" : "{", "(json structure) [#json_text]", 2);
		suggestionList.addRaw(inner ? "'[" : "[", "(json list) [#json_text]", 1);
	}

	private static void getJsonCompoundSuggestions(SuggestionList suggestionList, NbtSuggestion.ParentInfo parentInfo,
												   CustomTagParser.Type parserType)
	{
		suggestionList.addAll(NbtSuggestionManager.get("json_text/common"), parserType);
		String contentType = jsonTextInit(suggestionList, parentInfo, parserType);

		for (String str : JSON_TEXT_TYPE_SPECIFIC)
		{
			int priority = (contentType != null && !str.equals(contentType)) ? -1 : 0;
			suggestionList.addAll(NbtSuggestionManager.get("json_text/type_specific/" + str), parserType, priority);
		}

		int priority = (contentType != null && !JSON_TEXT_TYPE_HAS_SEPARATOR.contains(contentType)) ? -1 : 0;
		suggestionList.addAll(NbtSuggestionManager.get("json_text/compound/separator"), parserType, priority);
	}

	private static @Nullable String jsonTextInit(SuggestionList suggestionList, NbtSuggestion.ParentInfo parentInfo,
												 CustomTagParser.Type parserType)
	{
		NbtSuggestions initialContent = NbtSuggestionManager.get("json_text/initial_content");
		if (initialContent == null) { return null; }

		Map<String, String> tagMap = parentInfo.tagMap != null ? parentInfo.tagMap : Map.of();

		if (tagMap.containsKey("type"))
		{
			String valueType = tagMap.get("type");
			NbtSuggestion typeSuggestion = initialContent.get("type");
			NbtSuggestion valueSuggestion = initialContent.get(valueType);
			if (typeSuggestion != null) { suggestionList.add(new TagSuggestion(typeSuggestion, parserType, 100)); }
			if (valueSuggestion != null) { suggestionList.add(new TagSuggestion(valueSuggestion, parserType, 100)); }

			for (NbtSuggestion suggestion : initialContent.getAll())
			{
				if (suggestion == typeSuggestion || suggestion == valueSuggestion) { continue; }
				suggestionList.add(new TagSuggestion(suggestion, parserType, -1));
			}

			return valueType;
		}

		for (NbtSuggestion suggestion : initialContent.getAll())
		{
			if (tagMap.containsKey(suggestion.tag))
			{
				suggestionList.addAll(initialContent, parserType, -1);
				return jsonTextTypeFromTag(suggestion.tag);
			}
		}

		suggestionList.addAll(initialContent, parserType, 100);
		return null;
	}

	private static String jsonTextTypeFromTag(String tag)
	{
		return tag.equals("translate") ? "translatable" : tag;
	}

	private static void getHoverEventContentsSuggestions(SuggestionList suggestionList, NbtSuggestion.ParentInfo parentInfo,
														 @Nullable String data, CustomTagParser.Type parserType)
	{
		//TODO: improve suggestions for "show_text"
		if (parentInfo.parentTagMap == null || !parentInfo.parentTagMap.containsKey("action")) { return; }
		String hoverEvent = parentInfo.parentTagMap.get("action");
		switch (hoverEvent)
		{
			case "show_text" -> JSON_TEXT_COMPOUND.getSubtypeTagSuggestions(suggestionList, parentInfo, data, parserType);
			case "show_item" -> suggestionList.addAll(NbtSuggestionManager.get("json_text/compound/hover_event_show_item"), parserType);
			case "show_entity" -> suggestionList.addAll(NbtSuggestionManager.get("json_text/compound/hover_event_show_entity"), parserType);
		}
	}
}
