package com.mt1006.nbt_ac.autocomplete;

import com.mt1006.nbt_ac.autocomplete.suggestions.CustomSuggestion;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.MutablePair;

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
				else if (type.startsWith("tag/entity/")) { addEntityPredictions(suggestionList); }
			}

			setSubtype(suggestionList, "CustomName", NbtSuggestion.Subtype.JSON_TEXT, null);

			if (tag.endsWith("Item") || tag.endsWith("Items") || tag.equals("Book") || tag.equals("Trident") ||
					(rootTag.equals("Recipes") && (tag.equals("buy") || tag.equals("buyB") || tag.equals("sell"))) ||
					(tag.equals("Inventory") && !suggestionList.isEmpty()))
			{
				setSubtype(suggestionList, "id", NbtSuggestion.Subtype.REGISTRY_KEY, "minecraft:item");

				String id = findTag(tagList, "id", String.class);
				if (id != null) { setSubtype(suggestionList, "tag", NbtSuggestion.Subtype.TAG, "item/" + id); }
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
				Item blockItem = Registry.ITEM.get(new ResourceLocation(suggestion.subtypeData));
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
		Item item = Registry.ITEM.get(new ResourceLocation(itemName));

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
			entityTag.subtypeData = "entity/" + Registry.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(null));

			suggestionList.add(entityTag);
		}
	}

	private static void addEntityPredictions(List<CustomSuggestion> suggestionList)
	{
		setSubcompound(suggestionList, "Attributes", NbtSuggestionManager.get("subcompound/nbt_ac:attributes"));
		setSubcompound(suggestionList, "VillagerData", NbtSuggestionManager.get("subcompound/nbt_ac:villager_data"));


		NbtSuggestion nbtSuggestion = findNbtSuggestion(suggestionList, "Passengers");
		if (nbtSuggestion == null)
		{
			nbtSuggestion = new NbtSuggestion("Passengers", NbtSuggestion.Type.LIST, NbtSuggestion.SuggestionType.PREDICTION);
			nbtSuggestion.listType = NbtSuggestion.Type.COMPOUND;
			suggestionList.add(nbtSuggestion);
		}
	}

	private static void addTagPredictions(String tag, List<CustomSuggestion> suggestionList)
	{
		CustomTagParser tempTagParser = new CustomTagParser("");
		tempTagParser.stack.push(new ArrayList<>());
		tempTagParser.stack.peek().add(new MutablePair<>("$tag/" + tag, null));
		tempTagParser.stack.push(new ArrayList<>());

		addPredictions(tempTagParser, suggestionList, false);
	}

	private static void setSubtype(List<CustomSuggestion> suggestionList, String tag, NbtSuggestion.Subtype subtype, String subtypeData)
	{
		NbtSuggestion nbtSuggestion = findNbtSuggestion(suggestionList, tag);
		if (nbtSuggestion == null || nbtSuggestion.subtype != NbtSuggestion.Subtype.NONE) { return; }

		nbtSuggestion.subtype = subtype;
		nbtSuggestion.subtypeData = subtypeData;
		nbtSuggestion.changeSuggestionType(NbtSuggestion.SuggestionType.SUBTYPE_PREDICTION);
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
