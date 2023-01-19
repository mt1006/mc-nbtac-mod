package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.Disassembly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NbtSuggestion extends CustomSuggestion
{
	public String tag;
	public Type type;
	public Type listType;
	public Subtype subtype = Subtype.NONE;
	public String subtypeData = null;
	public NbtSuggestions subcompound = null;
	public SuggestionType suggestionType = SuggestionType.NORMAL;

	public NbtSuggestion(String tag, Type type)
	{
		this.tag = tag;
		this.type = type;
		this.listType = Type.UNKNOWN;
	}

	public NbtSuggestion(String tag, Type type, SuggestionType suggestionType)
	{
		this(tag, type);
		this.suggestionType = suggestionType;
	}

	public Type getType()
	{
		return type;
	}

	public Type getListType()
	{
		return listType;
	}

	public String getComplexTag()
	{
		String substring = "";

		if (subtype == Subtype.TAG)
		{
			substring = "tag/" + subtypeData;
		}

		return String.format("%s$%s", tag, substring);
	}

	public NbtSuggestions addSubcompound()
	{
		subcompound = new NbtSuggestions();
		return subcompound;
	}

	public <T> boolean getSubtypeSuggestions(List<CustomSuggestion> suggestionList)
	{
		switch (subtype)
		{
			case ENUM:
				if (subtypeData == null) { break; }
				suggestionList.clear();

				for (String substring : subtypeData.split(";"))
				{
					suggestionList.add(new SimpleSuggestion(substring, null));
				}
				return true;

			case DESCRIBED_ENUM:
				if (subtypeData == null) { break; }
				suggestionList.clear();

				String suggestionText = null;
				for (String substring : subtypeData.split(";"))
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
				if (subtypeData == null) { break; }

				try
				{
					ResourceLocation registryLocation = new ResourceLocation(subtypeData);
					Registry<T> registry = (Registry<T>)Registry.REGISTRY.get(registryLocation);
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
					suggestionList.add(new SimpleSuggestion(Integer.toString(i), subtext));
				}

				for (int i = 9; i < 35; i++)
				{
					int row = ((i - 9) / 9) + 1;
					int column = ((i - 9) % 9) + 1;
					String subtext = String.format("  §8<Storage %d:%d> [#inventory_slot]", row, column);
					suggestionList.add(new SimpleSuggestion(Integer.toString(i), subtext));
				}

				suggestionList.add(new SimpleSuggestion("100", "  §8<Feet> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("101", "  §8<Legs> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("102", "  §8<Chest> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("103", "  §8<Head> [#inventory_slot]"));
				suggestionList.add(new SimpleSuggestion("-106", "  §8<Off-hand> [#inventory_slot]"));
				return true;
		}

		return false;
	}

	public void changeSuggestionType(SuggestionType newSuggestionType)
	{
		if (suggestionType == SuggestionType.NORMAL) { suggestionType = newSuggestionType; }
	}

	@Override
	public String getSuggestionText()
	{
		return tag;
	}

	@Override
	public String getSuggestionSubtext()
	{
		return String.format("  §8%s[%s]", suggestionType.symbol, type.getName());
	}

	@Override
	public Message getSuggestionTooltip()
	{
		return new LiteralMessage(String.format("%s§r §8%s[%s]", tag, suggestionType.name, type.getName()));
	}

	public enum Type
	{
		NOT_FOUND((byte)-1),
		UNKNOWN((byte)-1),
		BOOLEAN((byte)-1),
		BYTE(Tag.TAG_BYTE),
		SHORT(Tag.TAG_SHORT),
		INT(Tag.TAG_INT),
		LONG(Tag.TAG_LONG),
		FLOAT(Tag.TAG_FLOAT),
		DOUBLE(Tag.TAG_DOUBLE),
		STRING(Tag.TAG_STRING),
		LIST(Tag.TAG_LIST),
		BYTE_ARRAY(Tag.TAG_BYTE_ARRAY),
		INT_ARRAY(Tag.TAG_INT_ARRAY),
		LONG_ARRAY(Tag.TAG_LONG_ARRAY),
		COMPOUND(Tag.TAG_COMPOUND),
		UUID((byte)-1);

		private static final HashMap<String, Type> nameMap = new HashMap<>();
		private static final HashMap<String, Type> methodNameMap = new HashMap<>();
		private static final HashMap<Byte, Type> idMap = new HashMap<>();
		private final byte id;

		Type(byte id)
		{
			this.id = id;
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
				for (Method method : Disassembly.findClass(CompoundTag.class.getName()).getMethods())
				{
					if (!method.isPublic()) { continue; }
					Type type = fromMethodObject(method);
					if (type != NOT_FOUND) { methodNameMap.put(method.getName(), type); }
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

		private static Type fromMethodObject(Method method)
		{
			String methodArguments = "(Ljava/lang/String;)";
			String signature = method.getSignature();

			if (!signature.startsWith(methodArguments))
			{
				String getListSignature = "(Ljava/lang/String;I)L" + ListTag.class.getName().replace('.', '/') + ";";
				if (method.getSignature().equals(getListSignature)) { return LIST; }
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

			Code code = method.getCode();
			String codeString = Utility.codeToString(code.getCode(), code.getConstantPool(), 0, -1);
			if (codeString.contains("(Ljava/lang/String;I)Z")) { return BYTE; }
			if (codeString.contains("(Ljava/lang/String;)B")) { return BOOLEAN; }

			return NOT_FOUND;
		}
	}

	public enum Subtype
	{
		NONE,               // subtypeData = null
		ENUM,               // subtypeData = "1;2;\"aaa\""
		DESCRIBED_ENUM,     // subtypeData = "1;description;2;description;3;;4"
		TAG,                // subtypeData = "item/minecraft:compass"
		BLOCK_STATE_TAG,    // subtypeData = "minecraft:chest"
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
		NORMAL("",""),
		PREDICTION("(*) ", "(prediction) "),
		SUBTYPE_PREDICTION("(S) ", "(subtype prediction) "),
		COMPOUND_PREDICTION("(C) ", "(compound prediction) "),
		UNCERTAIN("(?) ", "(uncertain) ");

		public final String symbol;
		public final String name;

		SuggestionType(String symbol, String name)
		{
			this.symbol = symbol;
			this.name = name;
		}
	}
}
