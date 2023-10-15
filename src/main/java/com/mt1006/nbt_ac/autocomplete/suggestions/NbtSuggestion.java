package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.Disassembly;
import com.mt1006.nbt_ac.utils.ComparableLiteralMessage;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import com.mt1006.nbt_ac.utils.TagType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NbtSuggestion extends CustomSuggestion
{
	public final String tag;
	public Type type;
	public Type listType = Type.UNKNOWN;
	public Subtype subtype = Subtype.NONE;
	public @Nullable String subtypeData = null;
	public @Nullable String subtypeWith = null;
	public boolean subtypeWithParentTag = false;
	public @Nullable NbtSuggestions subcompound = null;
	public SuggestionType suggestionType = SuggestionType.NORMAL;

	public NbtSuggestion(String tag, Type type)
	{
		this.tag = tag;
		this.type = type;
	}

	public NbtSuggestion(String tag, Type type, SuggestionType suggestionType)
	{
		this(tag, type);
		this.suggestionType = suggestionType;
	}

	public NbtSuggestion copy(boolean prediction, NbtSuggestions oldParent, NbtSuggestions newParent)
	{
		NbtSuggestion newSuggestion = new NbtSuggestion(tag, type, suggestionType);
		newSuggestion.listType = listType;
		newSuggestion.subtype = subtype;
		newSuggestion.subtypeData = subtypeData;

		if (prediction) {newSuggestion.changeSuggestionType(SuggestionType.PREDICTION); }

		if (subcompound != null)
		{
			if (subcompound == oldParent)
			{
				newSuggestion.subcompound = newParent;
			}
			else
			{
				newSuggestion.subcompound = new NbtSuggestions();
				newSuggestion.subcompound.copyAll(subcompound, prediction);
			}
		}
		return newSuggestion;
	}

	public NbtSuggestions addSubcompound()
	{
		subcompound = new NbtSuggestions();
		return subcompound;
	}

	public <T> boolean getSubtypeSuggestions(List<CustomSuggestion> suggestionList, ParentInfo parentInfo)
	{
		String finalData = getFinalSubtypeData(parentInfo);

		switch (subtype)
		{
			case ENUM:
				if (finalData == null) { break; }
				suggestionList.clear();

				for (String substring : finalData.split(";"))
				{
					suggestionList.add(new SimpleSuggestion(substring, null));
				}
				return true;

			case DESCRIBED_ENUM:
				if (finalData == null) { break; }
				suggestionList.clear();

				String suggestionText = null;
				for (String substring : finalData.split(";"))
				{
					if (suggestionText == null)
					{
						suggestionText = substring;
					}
					else
					{
						suggestionList.add(new SimpleSuggestion(suggestionText, String.format("  §8<%s>", substring)));
						suggestionText = null;
					}
				}
				return true;

			case REGISTRY_KEY:
			case REGISTRY_ID:
				if (finalData == null) { break; }

				try
				{
					ResourceLocation registryLocation = new ResourceLocation(finalData);
					Registry<T> registry = (Registry<T>)RegistryUtils.REGISTRY.get(registryLocation);
					if (registry == null) { break; }

					suggestionList.clear();
					if (subtype == Subtype.REGISTRY_ID)
					{
						for (T object : registry)
						{
							suggestionList.add(new SimpleSuggestion(Integer.toString(registry.getId(object)),
									"  §8\"" + registry.getKey(object) + "\" [#" + registryLocation.getPath() + "]"));
						}
					}
					else
					{
						for (T object : registry)
						{
							suggestionList.add(new SimpleSuggestion(
									"\"" + registry.getKey(object) + "\"", "  §8[#" + registryLocation.getPath() + "]"));
						}
					}
				}
				catch (Exception ignore) {}
				return true;

			case RECIPE:
				ClientLevel level = Minecraft.getInstance().level;
				if (level == null) { break; }

				for (ResourceLocation id : level.getRecipeManager().getRecipeIds().toArray(ResourceLocation[]::new))
				{
					suggestionList.add(new SimpleSuggestion("\"" + id + "\"", null));
				}
				return true;

			case JSON_TEXT:
				suggestionList.clear();
				suggestionList.add(new SimpleSuggestion("' \"", "  §8[#json_text]"));
				return true;

			case RANDOM_UUID:
				suggestionList.clear();

				UUID randomUUID = UUID.randomUUID();
				int uuidInt0 = (int)randomUUID.getLeastSignificantBits();
				int uuidInt1 = (int)(randomUUID.getLeastSignificantBits() >>> 32);
				int uuidInt2 = (int)randomUUID.getMostSignificantBits();
				int uuidInt3 = (int)(randomUUID.getMostSignificantBits() >>> 32);

				String uuidString = String.format("[I;%d, %d, %d, %d]", uuidInt3, uuidInt2, uuidInt1, uuidInt0);
				suggestionList.add(new SimpleSuggestion(uuidString, "  §8[#random_uuid]"));
				return true;

			case INVENTORY_SLOT:
				suggestionList.clear();

				for (int i = 0; i < 9; i++)
				{
					String subtext = String.format("  §8<Hotbar %d> [#inventory_slot]", i + 1);
					suggestionList.add(new SimpleSuggestion(String.format("%d%s", i, type.suffix), subtext));
				}

				for (int i = 9; i < 35; i++)
				{
					int row = ((i - 9) / 9) + 1;
					int column = ((i - 9) % 9) + 1;
					String subtext = String.format("  §8<Storage %d:%d> [#inventory_slot]", row, column);
					suggestionList.add(new SimpleSuggestion(String.format("%d%s", i, type.suffix), subtext));
				}

				suggestionList.add(new SimpleSuggestion("100" + type.suffix, "  §8<Feet> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("101" + type.suffix, "  §8<Legs> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("102" + type.suffix, "  §8<Chest> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("103" + type.suffix, "  §8<Head> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("-106" + type.suffix, "  §8<Off-hand> [#inventory_slot]"));
				return true;
		}

		return false;
	}

	public <T extends Comparable<T>> void getSubtypeTagSuggestions(List<CustomSuggestion> suggestionList, ParentInfo parentInfo)
	{
		String finalData = getFinalSubtypeData(parentInfo);

		switch (subtype)
		{
			case TAG:
				if (finalData == null) { break; }
				finalData = finalData.replace("block/item/", "block/");
				finalData = finalData.replace("entity/item/", "entity/");

				NbtSuggestions tagSuggestions = NbtSuggestionManager.get(finalData);
				NbtSuggestionManager.addToList(suggestionList, tagSuggestions, finalData);
				break;

			case BLOCK_STATE_TAG:
				try
				{
					if (finalData == null) { break; }

					if (finalData.startsWith("block/")) { finalData = finalData.substring(6); }
					else if (finalData.startsWith("item/")) { finalData = finalData.substring(5); }

					Item blockItem = RegistryUtils.ITEM.get(new ResourceLocation(finalData));
					if (!(blockItem instanceof BlockItem)) { break; }

					for (Property<?> property : ((BlockItem)blockItem).getBlock().defaultBlockState().getProperties())
					{
						NbtSuggestion nbtSuggestion = new NbtSuggestion(property.getName(), NbtSuggestion.Type.STRING);
						nbtSuggestion.subtype = NbtSuggestion.Subtype.ENUM;

						StringBuilder enumStringBuilder = new StringBuilder();
						for (T possibleValue : ((Property<T>)property).getPossibleValues())
						{
							enumStringBuilder.append("\"").append(((Property<T>)property).getName(possibleValue)).append("\";");
						}
						nbtSuggestion.subtypeData = enumStringBuilder.toString();

						suggestionList.add(nbtSuggestion);
					}
				}
				catch (Exception ignore) {}
				break;

			case SPAWN_EGG:
				try
				{
					if (finalData == null) { break; }
					if (finalData.startsWith("item/")) { finalData = finalData.substring(5); }

					Item item = RegistryUtils.ITEM.get(new ResourceLocation(finalData));
					if (item instanceof SpawnEggItem)
					{
						String key = RegistryUtils.ENTITY_TYPE.getKey(((SpawnEggItem)item).getType(null)).toString();
						NbtSuggestions spawnEggSuggestions = NbtSuggestionManager.get("entity/" + key);
						NbtSuggestionManager.addToList(suggestionList, spawnEggSuggestions, finalData);
					}
				}
				catch (Exception ignore) {}
				break;
		}
	}

	public String getFinalTagName(ParentInfo parentInfo)
	{
		String finalData = getFinalSubtypeData(parentInfo);

		if (subtype == Subtype.TAG && finalData != null)
		{
			return finalData.replace("block/item/", "block/");
		}
		return tag;
	}

	private @Nullable String getFinalSubtypeData(ParentInfo parentInfo)
	{
		if (subtypeWith != null && subtypeData != null && subtypeData.contains("*"))
		{
			if (subtypeWith.equals("#parent"))
			{
				if (parentInfo.parentTag == null) { return null; }
				return subtypeData.replace("*", parentInfo.parentTag);
			}
			else if (subtypeWith.equals("#parent2"))
			{
				if (parentInfo.secondParentTag == null) { return null; }
				return subtypeData.replace("*", parentInfo.secondParentTag);
			}
			else
			{
				Map<String, String> map = subtypeWithParentTag ? parentInfo.parentTagMap : parentInfo.tagMap;
				if (map == null) { return null; }

				String tagValue = map.get(subtypeWith);
				if (tagValue == null) { return null; }

				return subtypeData.replace("*", tagValue);
			}
		}
		return subtypeData;
	}

	public void changeSuggestionType(SuggestionType newSuggestionType)
	{
		if (newSuggestionType.level >= suggestionType.level) { suggestionType = newSuggestionType; }
	}

	@Override public String getSuggestionText()
	{
		return tag;
	}

	@Override public String getSuggestionSubtext()
	{
		return String.format("  §8%s[%s]", suggestionType.symbol, type.getName());
	}

	@Override public Message getSuggestionTooltip()
	{
		return new ComparableLiteralMessage(String.format("%s§r §8%s[%s]", tag, suggestionType.name, type.getName()));
	}

	public enum Type
	{
		NOT_FOUND((byte)-1),
		UNKNOWN((byte)-1),
		BOOLEAN((byte)-1, "b"),
		BYTE(TagType.BYTE, "b"),
		SHORT(TagType.SHORT, "s"),
		INT(TagType.INT),
		LONG(TagType.LONG, "l"),
		FLOAT(TagType.FLOAT, "f"),
		DOUBLE(TagType.DOUBLE),
		STRING(TagType.STRING),
		LIST(TagType.LIST),
		BYTE_ARRAY(TagType.BYTE_ARRAY),
		INT_ARRAY(TagType.INT_ARRAY),
		LONG_ARRAY(TagType.LONG_ARRAY),
		COMPOUND(TagType.COMPOUND),
		UUID((byte)-1);

		private static final HashMap<String, Type> nameMap = new HashMap<>();
		private static final HashMap<String, Type> methodNameMap = new HashMap<>();
		private static final HashMap<Byte, Type> idMap = new HashMap<>();
		private final byte id;
		public final String suffix;

		Type(byte id)
		{
			this.id = id;
			this.suffix = "";
		}

		Type(byte id, String suffix)
		{
			this.id = id;
			this.suffix = suffix;
		}

		public String getName()
		{
			return name().toLowerCase();
		}

		public static void init()
		{
			for (Type type : values())
			{
				String typeName = type.getName();
				nameMap.put(typeName, type);
				idMap.put(type.id, type);
			}

			try
			{
				ClassNode classNode = Disassembly.loadClass(CompoundTag.class.getCanonicalName());
				for (MethodNode method : classNode.methods)
				{
					if ((method.access & Opcodes.ACC_PUBLIC) == 0) { continue; }
					Type type = fromMethodObject(classNode, method);
					if (type != NOT_FOUND) { methodNameMap.put(method.name, type); }
				}
			}
			catch (Exception ignore) {}
		}

		public static Type fromName(String name)
		{
			return nameMap.getOrDefault(name, NOT_FOUND);
		}

		public static Type fromMethodName(String name)
		{
			return methodNameMap.getOrDefault(name, NOT_FOUND);
		}

		public static Type fromID(byte id)
		{
			return idMap.getOrDefault(id, UNKNOWN);
		}

		private static Type fromMethodObject(ClassNode classNode, MethodNode method)
		{
			String methodArguments = "(Ljava/lang/String;)";
			String signature = method.desc;

			if (!signature.startsWith(methodArguments))
			{
				String getListSignature = "(Ljava/lang/String;I)L" + ListTag.class.getName().replace('.', '/') + ";";
				if (method.desc.equals(getListSignature)) { return LIST; }
				return NOT_FOUND;
			}

			String retTypeSignature = signature.substring(signature.indexOf(')') + 1);

			if (retTypeSignature.equals("L" + Tag.class.getName().replace('.', '/') + ";")) { return UNKNOWN; }
			if (retTypeSignature.equals("L" + CompoundTag.class.getName().replace('.', '/') + ";")) { return COMPOUND; }

			switch (retTypeSignature)
			{
				case "S": return SHORT;
				case "I": return INT;
				case "J": return LONG;
				case "F": return FLOAT;
				case "D": return DOUBLE;
				case "[B": return BYTE_ARRAY;
				case "[I": return INT_ARRAY;
				case "[J": return LONG_ARRAY;
				case "Ljava/lang/String;": return STRING;
				case "Ljava/util/UUID;": return UUID;

				case "B":
				case "Z":
					break;

				default: return NOT_FOUND;
			}

			try
			{
				Disassembly.ValueTracker valueTracker = new Disassembly.ValueTracker(null, false, true);
				Analyzer<Disassembly.TrackedValue> analyzer = new Analyzer<>(valueTracker);
				analyzer.analyze(classNode.name, method);

				for (Disassembly.InvokeInfo invokeInfo : valueTracker.invokes)
				{
					if (invokeInfo.insn.desc.equals("(Ljava/lang/String;I)Z")) { return BYTE; }
					if (invokeInfo.insn.desc.equals("(Ljava/lang/String;)B")) { return BOOLEAN; }
				}
			}
			catch (Exception ignore) {}

			return NOT_FOUND;
		}
	}

	public enum Subtype
	{
		NONE,               // subtypeData = null
		ENUM,               // subtypeData = "1;2;\"aaa\""
		DESCRIBED_ENUM,     // subtypeData = "1;description;2;description;3;;4"
		TAG,                // subtypeData = "item/minecraft:compass"
		BLOCK_STATE_TAG,    // subtypeData = "minecraft:chest" (can also start with "block/" or "item/")
		SPAWN_EGG,          // subtypeData = "minecraft:allay_spawn_egg" (can also start with "item/")
		REGISTRY_ID,        // subtypeData = "minecraft:mob_effect"
		REGISTRY_KEY,       // subtypeData = "minecraft:mob_effect"
		RECIPE,             // subtypeData = null
		ITEM_COMPOUND,      // subtypeData = null
		JSON_TEXT,          // subtypeData = null
		RANDOM_UUID,        // subtypeData = null
		INVENTORY_SLOT;     // subtypeData = null

		public String getName()
		{
			return name().toLowerCase();
		}

		public static Subtype fromName(String name)
		{
			for (Subtype subtype : values())
			{
				if (name.equals(subtype.getName())) { return subtype; }
			}
			return NONE;
		}
	}

	public enum SuggestionType
	{
		NORMAL("", "", 0),
		UNCERTAIN("(?) ", "(uncertain) ", 1),
		COMPOUND_PREDICTION("(C) ", "(compound prediction) ", 2),
		SUBTYPE_PREDICTION("(S) ", "(subtype prediction) ", 3),
		TYPE_PREDICTION("(T) ", "(type prediction) ", 4),
		PREDICTION("(*) ", "(prediction) ", 5);

		public final String symbol;
		public final String name;
		public final int level;

		SuggestionType(String symbol, String name, int level)
		{
			this.symbol = symbol;
			this.name = name;
			this.level = level;
		}
	}

	public static class ParentInfo
	{
		public @Nullable Map<String, String> tagMap;
		public @Nullable Map<String, String> parentTagMap;
		public @Nullable String parentTag;
		public @Nullable String secondParentTag;

		private ParentInfo(@Nullable Map<String, String> tagMap, @Nullable Map<String, String> parentTagMap,
						   @Nullable String parentTag, @Nullable String secondParentTag)
		{
			this.tagMap = tagMap;
			this.parentTagMap = parentTagMap;
			this.parentTag = parentTag;
			this.secondParentTag = secondParentTag;
		}

		public ParentInfo withTagMap(@Nullable Map<String, String> newTagMap)
		{
			return new ParentInfo(newTagMap, parentTagMap, parentTag, secondParentTag);
		}

		public ParentInfo createChild(@Nullable NbtSuggestion suggestion)
		{
			if (suggestion == null) { return new ParentInfo(new HashMap<>(), tagMap, "[undefined]", parentTag); }
			ParentInfo newParentInfo = new ParentInfo(new HashMap<>(), tagMap, suggestion.tag, parentTag);
			newParentInfo.parentTag = suggestion.getFinalTagName(newParentInfo);
			return newParentInfo;
		}

		public void putTag(String key, String value)
		{
			if (tagMap != null) { tagMap.put(key, value); }
		}

		public static ParentInfo fromRoot(@Nullable String rootTag)
		{
			return new ParentInfo(new HashMap<>(), null, rootTag, null);
		}

		public static ParentInfo blank()
		{
			return new ParentInfo(new HashMap<>(), null, null, null);
		}
	}
}
