package com.mt1006.nbt_ac.autocomplete;

import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Predictions
{
	public static void addPredictions(CustomTagParser tagParser, List<CustomSuggestion> suggestionList, boolean alreadyAdded)
	{
		try
		{
			ArrayList<MutablePair<String, Object>> previousTagList = tagParser.stack.get(tagParser.stack.size() - 2);
			ArrayList<MutablePair<String, Object>> tagList = tagParser.stack.get(tagParser.stack.size() - 1);

			String complexTag = previousTagList.get(previousTagList.size() - 1).left;
			String tag = complexTag.substring(0, complexTag.indexOf('$'));
			String type = complexTag.substring(complexTag.indexOf('$') + 1);

			String rootTag = "";
			String rootType = "";
			if (tagParser.stack.size() > 2)
			{
				ArrayList<MutablePair<String, Object>> rootTagList = tagParser.stack.get(tagParser.stack.size() - 3);
				String rootComplexTag = rootTagList.get(rootTagList.size() - 1).left;
				rootTag = rootComplexTag.substring(0, rootComplexTag.indexOf('$'));
				rootType = rootComplexTag.substring(rootComplexTag.indexOf('$') + 1);
			}

			boolean isEmpty = suggestionList.isEmpty();

			if (!alreadyAdded && type.startsWith("tag/"))
			{
				addCommonTags(type, suggestionList);
				if (type.startsWith("tag/item/")) { addItemPredictions(type.substring(9), suggestionList); }
				else if (type.startsWith("tag/entity/")) { addEntityPredictions(type.substring(11), suggestionList); }
			}

			setSubtype(suggestionList, "CustomName", NbtSuggestion.Subtype.JSON_TEXT, null);

			if (tag.endsWith("Item") || tag.endsWith("Items") || tag.equals("Book") || tag.equals("Trident") ||
					(rootTag.equals("Recipes") && (tag.equals("buy") || tag.equals("buyB") || tag.equals("sell"))) ||
					(tag.equals("Inventory") && !suggestionList.isEmpty()) || tag.equals("item"))
			{
				setSubtype(suggestionList, "id", NbtSuggestion.Subtype.REGISTRY_KEY, "minecraft:item");

				String id = findTag(tagList, "id", String.class);
				if (id != null) { setSubtype(suggestionList, "tag", NbtSuggestion.Subtype.TAG, "item/" + id); }
			}
			else if (tag.equals("block_state"))
			{
				setFullType(suggestionList, "Name", NbtSuggestion.Type.STRING, NbtSuggestion.Subtype.REGISTRY_KEY, "minecraft:block");

				String id = findTag(tagList, "Name", String.class);
				if (id != null) { setSubtype(suggestionList, "Properties", NbtSuggestion.Subtype.BLOCK_STATE_TAG, id); }
			}
			else if (tag.equals("Inventory"))
			{
				if (!alreadyAdded)
				{
					NbtSuggestion idSuggestion = new NbtSuggestion("id", NbtSuggestion.Type.STRING, NbtSuggestion.SuggestionType.PREDICTION);
					idSuggestion.subtype = NbtSuggestion.Subtype.REGISTRY_KEY;
					idSuggestion.subtypeData = "minecraft:item";
					suggestionList.add(idSuggestion);

					suggestionList.add(new NbtSuggestion("Count", NbtSuggestion.Type.BYTE, NbtSuggestion.SuggestionType.PREDICTION));

					NbtSuggestion tagSuggestion = new NbtSuggestion(
							"tag", NbtSuggestion.Type.COMPOUND, NbtSuggestion.SuggestionType.PREDICTION);
					suggestionList.add(tagSuggestion);
				}

				String id = findTag(tagList, "id", String.class);
				if (id != null) { setSubtype(suggestionList, "tag", NbtSuggestion.Subtype.TAG, "item/" + id); }

				if (rootType.equals("tag/entity/minecraft:player") && !alreadyAdded)
				{
					NbtSuggestion slotSuggestion =
							new NbtSuggestion("Slot", NbtSuggestion.Type.BYTE, NbtSuggestion.SuggestionType.PREDICTION);
					slotSuggestion.subtype = NbtSuggestion.Subtype.INVENTORY_SLOT;
					suggestionList.add(slotSuggestion);
				}
			}
			else if (!alreadyAdded && tag.endsWith("Effects"))
			{
				setSubtype(suggestionList, "Id", NbtSuggestion.Subtype.REGISTRY_ID, "minecraft:mob_effect");

				Object previousNbtSuggestions = previousTagList.get(previousTagList.size() - 1).right;
				if (previousNbtSuggestions instanceof NbtSuggestions)
				{
					setSubcompound(suggestionList, "HiddenEffect", (NbtSuggestions)previousNbtSuggestions);
				}
			}
			else if (tag.equals("Passengers"))
			{
				String id = findTag(tagList, "id", String.class);

				if (id == null && !alreadyAdded)
				{
					NbtSuggestion nbtSuggestion = new NbtSuggestion("id", NbtSuggestion.Type.STRING);
					nbtSuggestion.subtype = NbtSuggestion.Subtype.REGISTRY_KEY;
					nbtSuggestion.subtypeData = "minecraft:entity_type";
					suggestionList.add(nbtSuggestion);
				}
				else if (tagList.size() == 1 && suggestionList.isEmpty())
				{
					NbtSuggestions nbtSuggestions = NbtSuggestionManager.get("entity/" + id);
					if (nbtSuggestions != null) { suggestionList.addAll(nbtSuggestions.suggestions); }
					addTagPredictions("entity/" + id, suggestionList);
				}
			}
			else if (!alreadyAdded && tag.equals("Leash"))
			{
				if (isEmpty)
				{
					suggestionList.add(new NbtSuggestion("UUID", NbtSuggestion.Type.UUID, NbtSuggestion.SuggestionType.PREDICTION));
					suggestionList.add(new NbtSuggestion("X", NbtSuggestion.Type.INT, NbtSuggestion.SuggestionType.PREDICTION));
					suggestionList.add(new NbtSuggestion("Y", NbtSuggestion.Type.INT, NbtSuggestion.SuggestionType.PREDICTION));
					suggestionList.add(new NbtSuggestion("Z", NbtSuggestion.Type.INT, NbtSuggestion.SuggestionType.PREDICTION));
				}
			}
			else if (!alreadyAdded && tag.equals("Attributes"))
			{
				if (isEmpty)
				{
					suggestionList.add(new NbtSuggestion("UUID", NbtSuggestion.Type.UUID, NbtSuggestion.SuggestionType.PREDICTION));
					suggestionList.add(new NbtSuggestion("X", NbtSuggestion.Type.INT, NbtSuggestion.SuggestionType.PREDICTION));
					suggestionList.add(new NbtSuggestion("Y", NbtSuggestion.Type.INT, NbtSuggestion.SuggestionType.PREDICTION));
					suggestionList.add(new NbtSuggestion("Z", NbtSuggestion.Type.INT, NbtSuggestion.SuggestionType.PREDICTION));
				}
			}
		}
		catch (Exception ignore) {}
	}

	public static <T extends Comparable<T>> void addSubtypeSuggestions(NbtSuggestion suggestion, List<CustomSuggestion> suggestionList)
	{
		switch (suggestion.subtype)
		{
			case TAG:
				NbtSuggestions tagSuggestions = NbtSuggestionManager.get(suggestion.subtypeData);
				if (tagSuggestions != null) { suggestionList.addAll(tagSuggestions.suggestions); }
				addTagPredictions(suggestion.subtypeData, suggestionList);
				break;

			case BLOCK_STATE_TAG:
				Item blockItem = BuiltInRegistries.ITEM.get(new ResourceLocation(suggestion.subtypeData));
				if (!(blockItem instanceof BlockItem)) { break; }

				for (Property<?> property : ((BlockItem)blockItem).getBlock().defaultBlockState().getProperties())
				{
					NbtSuggestion nbtSuggestion = new NbtSuggestion(property.getName(), NbtSuggestion.Type.STRING);
					nbtSuggestion.subtype = NbtSuggestion.Subtype.ENUM;

					StringBuilder enumStringBuilder = new StringBuilder();
					for (T possibleValue : ((Property<T>)property).getPossibleValues())
					{
						enumStringBuilder.append("\"" + ((Property<T>)property).getName(possibleValue) + "\";");
					}
					nbtSuggestion.subtypeData = enumStringBuilder.toString();

					suggestionList.add(nbtSuggestion);
				}
				break;
		}
	}

	private static void addCommonTags(String tag, List<CustomSuggestion> suggestionList)
	{
		int firstSlashIndex = tag.indexOf("/");
		int lastSlashIndex = tag.lastIndexOf("/");
		if (firstSlashIndex == -1 || lastSlashIndex == -1) { return; }

		String group = tag.substring(firstSlashIndex + 1, lastSlashIndex);
		NbtSuggestions commonSuggestions = NbtSuggestionManager.get("common/" + group);
		if (commonSuggestions == null) { return; }

		suggestionList.addAll(commonSuggestions.suggestions);
	}

	private static void addItemPredictions(String itemName, List<CustomSuggestion> suggestionList)
	{
		Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemName));

		if (item instanceof BlockItem)
		{
			NbtSuggestion blockEntityTag = new NbtSuggestion("BlockEntityTag", NbtSuggestion.Type.COMPOUND);
			blockEntityTag.subtype = NbtSuggestion.Subtype.TAG;
			blockEntityTag.subtypeData = "block/" + itemName;

			NbtSuggestion blockStateTag = new NbtSuggestion("BlockStateTag", NbtSuggestion.Type.COMPOUND);
			blockStateTag.subtype = NbtSuggestion.Subtype.BLOCK_STATE_TAG;
			blockStateTag.subtypeData = itemName;

			suggestionList.add(blockEntityTag);
			suggestionList.add(blockStateTag);
		}
		else if (item instanceof ShieldItem)
		{
			NbtSuggestion blockEntityTag = new NbtSuggestion("BlockEntityTag", NbtSuggestion.Type.COMPOUND);
			blockEntityTag.subtype = NbtSuggestion.Subtype.TAG;
			blockEntityTag.subtypeData = "block/minecraft:banner";

			suggestionList.add(blockEntityTag);
		}
		else if (item instanceof SpawnEggItem)
		{
			NbtSuggestion entityTag = new NbtSuggestion("EntityTag", NbtSuggestion.Type.COMPOUND);
			entityTag.subtype = NbtSuggestion.Subtype.TAG;
			entityTag.subtypeData = "entity/" + BuiltInRegistries.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(null));

			suggestionList.add(entityTag);
		}
	}

	private static void addEntityPredictions(String entityName, List<CustomSuggestion> suggestionList)
	{
		setSubcompound(suggestionList, "Attributes", NbtSuggestionManager.get("subcompound/nbt_ac:attributes"));
		setSubcompound(suggestionList, "VillagerData", NbtSuggestionManager.get("subcompound/nbt_ac:villager_data"));

		if (entityName.equals("block_display") || entityName.equals("minecraft:block_display") ||
				entityName.equals("item_display") || entityName.equals("minecraft:item_display") ||
				entityName.equals("text_display") || entityName.equals("minecraft:text_display"))
		{
			addDisplayEntityPredictions(suggestionList);
		}
		else if (entityName.equals("interaction") || entityName.equals("minecraft:interaction"))
		{
			addInteractionEntityPredictions(suggestionList);
		}

		NbtSuggestion nbtSuggestion = findNbtSuggestion(suggestionList, "Passengers");
		if (nbtSuggestion == null)
		{
			nbtSuggestion = new NbtSuggestion("Passengers", NbtSuggestion.Type.LIST, NbtSuggestion.SuggestionType.PREDICTION);
			nbtSuggestion.listType = NbtSuggestion.Type.COMPOUND;
			suggestionList.add(nbtSuggestion);
		}
	}

	private static void addDisplayEntityPredictions(List<CustomSuggestion> suggestionList)
	{
		// Common
		setType(suggestionList, "glow_color_override", NbtSuggestion.Type.INT);
		setType(suggestionList, "height", NbtSuggestion.Type.INT);
		setType(suggestionList, "width", NbtSuggestion.Type.INT);
		setType(suggestionList, "interpolation_duration", NbtSuggestion.Type.INT);
		setType(suggestionList, "start_interpolation", NbtSuggestion.Type.INT);
		setType(suggestionList, "shadow_radius", NbtSuggestion.Type.FLOAT);
		setType(suggestionList, "shadow_strength", NbtSuggestion.Type.FLOAT);
		setType(suggestionList, "view_range", NbtSuggestion.Type.FLOAT);
		setFullType(suggestionList, "billboard", NbtSuggestion.Type.STRING,
				NbtSuggestion.Subtype.ENUM, "\"fixed\";\"vertical\";\"horizontal\";\"center\"");
		setSubcompound(suggestionList, "brightness", NbtSuggestionManager.get("subcompound/nbt_ac:display_brightness"));
		setSubcompound(suggestionList, "transformation", NbtSuggestionManager.get("subcompound/nbt_ac:display_transformation"));

		// Item display
		setFullType(suggestionList, "item_display", NbtSuggestion.Type.STRING, NbtSuggestion.Subtype.ENUM,
				"\"none\";\"thirdperson_lefthand\";\"thirdperson_righthand\";\"firstperson_lefthand\";" +
				"\"firstperson_righthand\";\"head\";\"gui\";\"ground\";\"fixed\"");

		// Text display
		setType(suggestionList, "background", NbtSuggestion.Type.INT);
		setType(suggestionList, "line_width", NbtSuggestion.Type.INT);
		setType(suggestionList, "text_opacity", NbtSuggestion.Type.BYTE);
		setType(suggestionList, "default_background", NbtSuggestion.Type.BOOLEAN);
		setType(suggestionList, "see_through", NbtSuggestion.Type.BOOLEAN);
		setType(suggestionList, "shadow", NbtSuggestion.Type.BOOLEAN);
		setFullType(suggestionList, "text", NbtSuggestion.Type.STRING, NbtSuggestion.Subtype.JSON_TEXT, null);
		setFullType(suggestionList, "alignment", NbtSuggestion.Type.STRING, NbtSuggestion.Subtype.ENUM, "\"center\";\"left\";\"right\"");
	}

	private static void addInteractionEntityPredictions(List<CustomSuggestion> suggestionList)
	{
		setType(suggestionList, "width", NbtSuggestion.Type.FLOAT);
		setType(suggestionList, "height", NbtSuggestion.Type.FLOAT);
		setType(suggestionList, "response", NbtSuggestion.Type.BOOLEAN);
		setSubcompound(suggestionList, "attack", NbtSuggestionManager.get("subcompound/nbt_ac:interaction_info"));
		setSubcompound(suggestionList, "interaction", NbtSuggestionManager.get("subcompound/nbt_ac:interaction_info"));
	}

	private static void addTagPredictions(String tag, List<CustomSuggestion> suggestionList)
	{
		CustomTagParser tempTagParser = new CustomTagParser("");
		tempTagParser.stack.push(new ArrayList<>());
		tempTagParser.stack.peek().add(new MutablePair<>("$tag/" + tag, null));
		tempTagParser.stack.push(new ArrayList<>());

		addPredictions(tempTagParser, suggestionList, false);
	}

	private static void setType(List<CustomSuggestion> suggestionList, String tag, NbtSuggestion.Type type)
	{
		setFullType(suggestionList, tag, type, null, null);
	}

	private static void setSubtype(List<CustomSuggestion> suggestionList, String tag,
								   NbtSuggestion.Subtype subtype, @Nullable String subtypeData)
	{
		setFullType(suggestionList, tag, null, subtype, subtypeData);
	}

	private static void setFullType(List<CustomSuggestion> suggestionList, String tag, @Nullable NbtSuggestion.Type type,
										  @Nullable NbtSuggestion.Subtype subtype, @Nullable String subtypeData)
	{
		NbtSuggestion nbtSuggestion = findNbtSuggestion(suggestionList, tag);
		if (nbtSuggestion == null) { return; }

		if (type != null && nbtSuggestion.type == NbtSuggestion.Type.UNKNOWN)
		{
			nbtSuggestion.type = type;
			nbtSuggestion.changeSuggestionType(NbtSuggestion.SuggestionType.TYPE_PREDICTION);
		}

		if (subtype != null && nbtSuggestion.subtype == NbtSuggestion.Subtype.NONE)
		{
			nbtSuggestion.subtype = subtype;
			nbtSuggestion.subtypeData = subtypeData;
			nbtSuggestion.changeSuggestionType(NbtSuggestion.SuggestionType.SUBTYPE_PREDICTION);
		}
	}

	private static void setSubcompound(List<CustomSuggestion> suggestionList, String tag, NbtSuggestions subcompound)
	{
		NbtSuggestion nbtSuggestion = findNbtSuggestion(suggestionList, tag);
		if (nbtSuggestion == null || (nbtSuggestion.subcompound != null && nbtSuggestion.subcompound.suggestions.size() != 0)) { return; }

		if (nbtSuggestion.type == NbtSuggestion.Type.UNKNOWN) { nbtSuggestion.type = NbtSuggestion.Type.COMPOUND; }
		nbtSuggestion.subcompound = subcompound;
		nbtSuggestion.changeSuggestionType(NbtSuggestion.SuggestionType.COMPOUND_PREDICTION);
	}

	private static NbtSuggestion findNbtSuggestion(List<CustomSuggestion> suggestionList, String tag)
	{
		for (CustomSuggestion suggestion : suggestionList)
		{
			if (!(suggestion instanceof NbtSuggestion)) { continue; }
			NbtSuggestion nbtSuggestion = (NbtSuggestion)suggestion;

			if (nbtSuggestion.tag.equals(tag)) { return nbtSuggestion; }
		}

		return null;
	}

	private static <T> T findTag(ArrayList<MutablePair<String, Object>> tagList, String tag, Class<T> clazz)
	{
		for (MutablePair<String, Object> pair : tagList)
		{
			String str = pair.getLeft();
			if (str.substring(0, str.indexOf('$')).equals(tag))
			{
				if (clazz.isAssignableFrom(pair.getRight().getClass())) { return (T)pair.getRight(); }
				else { return null; }
			}
		}
		return null;
	}
}
