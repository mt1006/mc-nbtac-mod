package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mt1006.nbt_ac.autocomplete.*;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.mixin.fields.*;
import com.mt1006.nbt_ac.utils.Fields;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum NbtSuggestionSubtype
{
	//TODO: improve json text scoreboard value suggestions
	//TODO: improve "type":"nbt" suggestions in json text
	NONE,                 // subtypeData = null
	ENUM,                 // subtypeData = "1;2;aaa"
	DESCRIBED_ENUM,       // subtypeData = "1;description;2;description;3;;4"
	BLOCK_STATE_ENUM,     // subtypeData = "up;down"
	ORDERED_ENUM,         // subtypeData = "first;second;third"
	REQUIRED_ID,          // subtypeData = "minecraft:furnace"
	TAG,                  // subtypeData = "item/minecraft:compass"
	TAG_WITH_ID,          // subtypeData = "entity/minecraft:bee"
	BLOCK_STATE_TAG,      // subtypeData = "minecraft:chest" (can also start with "block/" or "item/")
	SPAWN_EGG,            // subtypeData = "minecraft:allay_spawn_egg" (can also start with "item/")
	REGISTRY_ID,          // subtypeData = "minecraft:mob_effect"
	REGISTRY_KEY,         // subtypeData = "minecraft:mob_effect"
	ITEM_COMPONENTS,      // subtypeData = "minecraft:chest"
	RECIPE,               // subtypeData = null
	LOOT_TABLE,           // subtypeData = null
	BANNER_PATTERN,       // subtypeData = null
	MAP_DECORATION_TYPE,  // subtypeData = null
	FONT,                 // subtypeData = null
	RANDOM_UUID,          // subtypeData = null
	LONG_SEED,            // subtypeData = null
	INVENTORY_SLOT,       // subtypeData = null
	TRANSLATION_KEY,      // subtypeData = null
	KEYBIND,              // subtypeData = null //TODO: do something about suggestion list going out of the screen
	JSON_TEXT,            // subtypeData = null
	JSON_TEXT_COMPOUND,   // subtypeData = null
	JSON_TEXT_COLOR,      // subtypeData = null
	JSON_STYLE_COMPOUND,  // subtypeData = null
	ENTITY_SELECTOR,      // subtypeData = null
	HOVER_EVENT,          // subtypeData = "show_text" //TODO: replace with more generic subtype
	CLICK_EVENT,          // subtypeData = "open_url" //TODO: replace with more generic subtype
	DYE_COLOR,            // subtypeData = null
	EMPTY_COMPOUND,       // subtypeData = null
	ENCHANTMENTS,         // subtypeData = null
	MAP_DECORATIONS,      // subtypeData = null
	POT_DECORATION,       // subtypeData = null
	TRIM_PATTERN,         // subtypeData = null
	TRIM_MATERIAL,        // subtypeData = null
	JUKEBOX_SONG,         // subtypeData = null
	DAMAGE_TYPE_TAG;      // subtypeData = null

	private static final List<String> JSON_TEXT_TYPE_SPECIFIC = List.of("nbt", "translatable");
	private static final List<String> JSON_TEXT_TYPE_HAS_SEPARATOR = List.of("nbt", "selector");
	private static final Random rng = new Random();

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
		catch (IllegalArgumentException e) { return NONE; }
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

			case BLOCK_STATE_ENUM:
				if (data == null) { break; }
				suggestionList.clear();
				char quote = ModConfig.getDefaultQuotationMark(false);

				for (String substring : data.split(";"))
				{
					suggestionList.addRaw(String.format("%c%s%c", quote, substring, quote), null);
				}
				return true;

			case ORDERED_ENUM:
				if (data == null) { break; }
				suggestionList.clear();

				int priority = 99;
				for (String substring : data.split(";"))
				{
					suggestionList.add(CustomSuggestion.fromType(substring, null, suggestion.type, parserType, priority--));
				}
				return true;

			case REQUIRED_ID:
				if (data == null) { break; }
				suggestionList.clear();
				suggestionList.add(new IdSuggestion(ResourceLocation.parse(data), "#[required_id]", parserType));
				return true;

			case REGISTRY_KEY:
			case REGISTRY_ID:
				if (data == null) { break; }

				try
				{
					ResourceLocation registryLocation = ResourceLocation.parse(data);
					Holder.Reference<?> ref = RegistryUtils.REGISTRY.get(registryLocation).orElse(null);
					Registry<T> registry = ref != null ? (Registry<T>)ref.value() : null;
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
				MinecraftServer recipeServer = Minecraft.getInstance().getSingleplayerServer();
				if (recipeServer == null) { break; }

				suggestionList.clear();
				for (RecipeHolder<?> recipeHolder : recipeServer.getRecipeManager().getRecipes().toArray(RecipeHolder[]::new))
				{
					suggestionList.add(new IdSuggestion(recipeHolder.id().location(), null, parserType));
				}
				return true;

			case LOOT_TABLE:
				MinecraftServer singlePlayerServer = Minecraft.getInstance().getSingleplayerServer();
				if (singlePlayerServer == null) { break; }

				Optional<? extends HolderLookup.RegistryLookup<LootTable>> lootTableReg =
						singlePlayerServer.reloadableRegistries().lookup().lookup(Registries.LOOT_TABLE);
				if (lootTableReg.isEmpty()) { break; }

				suggestionList.clear();
				for (ResourceKey<LootTable> id : lootTableReg.get().listElementIds().toList())
				{
					suggestionList.add(new IdSuggestion(id.location(), null, parserType));
				}
				return true;

			case BANNER_PATTERN:
				suggestionList.clear();
				List<ResourceKey> bannerPatterns = Fields.getStaticFields(BannerPatterns.class, ResourceKey.class);
				bannerPatterns.forEach((key) -> suggestionList.add(new IdSuggestion(key.location(), "[#banner_pattern]", parserType)));
				return true;

			case MAP_DECORATION_TYPE:
				suggestionList.clear();
				List<Holder> decorationTypes = Fields.getStaticFields(MapDecorationTypes.class, Holder.class);
				for (Holder<ResourceKey> holder : decorationTypes)
				{
					ResourceKey<?> key = holder.unwrapKey().orElse(null);
					CustomSuggestion customSuggestion = key != null
							? new IdSuggestion(key.location(), "[#banner_pattern]", parserType)
							: new StringSuggestion("_error", null, parserType, 9999);
					suggestionList.add(customSuggestion);
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

			case LONG_SEED:
				suggestionList.clear();
				long random = rng.nextLong();
				if (random == 0) { random = 123; } // there's about 1/10^19 it will happen, but it may happen

				suggestionList.addRaw("0", "(random) [#seed]", 1);
				suggestionList.addRaw(random + "l", "(constant) [#seed]");
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

			case TRANSLATION_KEY:
				//suggestionList.clear();
				//TODO: implement
				return false;

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
				suggestionList.add(new StringSuggestion("@n", "[#entity_selector]", parserType)); //TODO: (PORT) remove it for 1.20.6
				return true;

			case DYE_COLOR:
				suggestionList.clear();
				for (DyeColor color : DyeColor.values())
				{
					suggestionList.add(new StringSuggestion(color.getName(), "[#dye_color]", parserType));
				}
				return true;

			case EMPTY_COMPOUND:
				suggestionList.clear();
				suggestionList.addRaw("{}", "[#empty_compound]");
				return true;

			case POT_DECORATION:
				suggestionList.clear();
				suggestionList.add(new IdSuggestion(RegistryUtils.ITEM.getKey(Items.BRICK), "[#pot_decoration]", parserType, 1));

				Map<Item, ResourceKey<String>> itemToPotTexture = DecoratedPotPatternsFields.getITEM_TO_POT_TEXTURE();
				if (itemToPotTexture == null) { return true; }

				for (Item item : itemToPotTexture.keySet())
				{
					suggestionList.add(new IdSuggestion(RegistryUtils.ITEM.getKey(item), "[#pot_decoration]", parserType));
				}
				return true;

			case TRIM_PATTERN:
				suggestionList.clear();
				List<ResourceKey> trimPatterns = Fields.getStaticFields(TrimPatterns.class, ResourceKey.class);
				trimPatterns.forEach((key) -> suggestionList.add(new IdSuggestion(key.location(), "[#trim_pattern]", parserType)));
				return true;

			case TRIM_MATERIAL:
				suggestionList.clear();
				List<ResourceKey> trimMaterial = Fields.getStaticFields(TrimMaterials.class, ResourceKey.class);
				trimMaterial.forEach((key) -> suggestionList.add(new IdSuggestion(key.location(), "[#trim_material]", parserType)));
				return true;

			case JUKEBOX_SONG:
				suggestionList.clear();
				List<ResourceKey> jukeboxSong = Fields.getStaticFields(JukeboxSongs.class, ResourceKey.class);
				jukeboxSong.forEach((key) -> suggestionList.add(new IdSuggestion(key.location(), "[#jukebox_song]", parserType)));
				return true;

			case DAMAGE_TYPE_TAG:
				suggestionList.clear();
				List<TagKey> damageTypes = Fields.getStaticFields(DamageTypeTags.class, TagKey.class);
				//TODO: fix, use IdSuggestion
				damageTypes.forEach((key) -> suggestionList.add(new StringSuggestion("#" + key.location(), "[#damage_type]", parserType)));
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
			case TAG_WITH_ID:
				if (data == null) { break; }
				data = data.replace("block/item/", "block/");
				data = data.replace("entity/item/", "entity/");

				NbtSuggestions tagSuggestions = NbtSuggestionManager.get(data);
				suggestionList.addAll(tagSuggestions, data, parserType);

				if (this == TAG_WITH_ID)
				{
					int dataSlashPos = data.indexOf('/');
					if (dataSlashPos == -1) { break; }

					NbtSuggestion tempSuggestion = new NbtSuggestion("id", NbtSuggestion.Type.STRING);
					tempSuggestion.subtype = REQUIRED_ID;
					tempSuggestion.subtypeData = data.substring(dataSlashPos + 1);
					suggestionList.add(new TagSuggestion(tempSuggestion, parserType, 100));
				}
				break;

			case BLOCK_STATE_TAG:
				try
				{
					if (data == null) { break; }

					if (data.startsWith("block/")) { data = data.substring(6); }
					else if (data.startsWith("item/")) { data = data.substring(5); }

					Item blockItem = RegistryUtils.ITEM.get(ResourceLocation.parse(data));
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

					Item item = RegistryUtils.ITEM.get(ResourceLocation.parse(data));
					if (item instanceof SpawnEggItem)
					{
						String key = RegistryUtils.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(new ItemStack(item))).toString();
						NbtSuggestions spawnEggSuggestions = NbtSuggestionManager.get("entity/" + key);
						suggestionList.addAll(spawnEggSuggestions, data, parserType);
					}
				}
				catch (Exception ignore) {}
				break;

			case ITEM_COMPONENTS:
				if (data == null) { break; }
				DataComponentManager.loadSuggestions(suggestionList, "", Set.of(), RegistryUtils.ITEM.get(data), parserType, false);
				break;

			case JSON_TEXT_COMPOUND:
				getJsonCompoundSuggestions(suggestionList, parentInfo, parserType);
				break;

			case JSON_STYLE_COMPOUND:
				suggestionList.addAll(NbtSuggestionManager.get("json_text/style"), parserType);
				break;

			case HOVER_EVENT:
				getHoverEventSuggestions(suggestionList, data, parserType);
				break;

			case CLICK_EVENT:
				getClickEventSuggestions(suggestionList, data, parserType);
				break;

			case ENCHANTMENTS:
				suggestionList.clear();
				List<ResourceKey> enchantments = Fields.getStaticFields(Enchantments.class, ResourceKey.class);
				for (ResourceKey<?> resourceKey : enchantments)
				{
					ResourceLocation id = resourceKey.location();
					NbtSuggestion tempSuggestion = new NbtSuggestion(id.toString(), NbtSuggestion.Type.INT);
					suggestionList.add(new TagIdSuggestion(tempSuggestion, id, parserType, true));
				}
				break;

			case MAP_DECORATIONS:
				// 36^6 > 2^31
				String decorationId = Long.toString(Math.abs(rng.nextInt()), Math.min(Character.MAX_RADIX, 36));
				NbtSuggestion tempSuggestion = new NbtSuggestion(decorationId, NbtSuggestion.Type.COMPOUND);
				tempSuggestion.subtype = TAG;
				tempSuggestion.subtypeData = "compound/nbt_ac:map_decoration";
				suggestionList.add(new TagSuggestion(tempSuggestion, parserType));

				if (parentInfo.tagMap == null) { break; }
				for (String tag : parentInfo.tagMap.keySet())
				{
					NbtSuggestion tempOldSuggestion = new NbtSuggestion(tag, NbtSuggestion.Type.COMPOUND);
					tempOldSuggestion.subtype = TAG;
					tempOldSuggestion.subtypeData = "compound/nbt_ac:map_decoration";
					suggestionList.add(new TagSuggestion(tempOldSuggestion, parserType));
				}
				break;
		}
	}

	//TODO: move json-related methods to a new class
	public static void getJsonTextPrefixSuggestions(SuggestionList suggestionList, boolean inner)
	{
		suggestionList.addRaw(ModConfig.getDefaultQuotationMarkStr(false), "(simple string) [#text_component]", 3);
		suggestionList.addRaw("{", "(text component) [#text_component]", 2);
		suggestionList.addRaw("[", "(test component list) [#text_component]", 1);
	}

	private static void getJsonCompoundSuggestions(SuggestionList suggestionList, NbtSuggestion.ParentInfo parentInfo,
												   CustomTagParser.Type parserType)
	{
		suggestionList.addAll(NbtSuggestionManager.get("json_text/common"), parserType);
		suggestionList.addAll(NbtSuggestionManager.get("json_text/style"), parserType);
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

	private static void getHoverEventSuggestions(SuggestionList suggestionList, @Nullable String data, CustomTagParser.Type parserType)
	{
		String compoundPath = switch (data)
		{
			case "show_text" -> "json_text/compound/hover_event_show_text";
			case "show_item" -> "json_text/compound/hover_event_show_item";
			case "show_entity" -> "json_text/compound/hover_event_show_entity";
			case null, default -> null;
		};
		if (compoundPath != null) { suggestionList.addAll(NbtSuggestionManager.get(compoundPath), parserType); }
	}

	private static void getClickEventSuggestions(SuggestionList suggestionList, @Nullable String data, CustomTagParser.Type parserType)
	{
		String tagName = switch (data)
		{
			case "open_url" -> "url";
			case "open_file" -> "path";
			case "run_command", "suggest_command" -> "command"; //TODO: suggestions for commands
			case "change_page" -> "page";
			case "copy_to_clipboard" -> "value";
			case null, default -> null;
		};

		if (tagName != null)
		{
			NbtSuggestion.Type type = data.equals("change_page") ? NbtSuggestion.Type.INT : NbtSuggestion.Type.STRING;
			suggestionList.add(new TagSuggestion(new NbtSuggestion(tagName, type), parserType));
		}
	}
}
