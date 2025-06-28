package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.SuggestionList;
import com.mt1006.nbt_ac.utils.ComparableLiteralMessage;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NbtSuggestion
{
	private static final NbtSuggestion DUMMY_COMPOUND = new NbtSuggestion("nbt_ac:dummy", Type.COMPOUND);
	public static int createdInstanceCounter = 0;
	public final String tag;
	public Type type;
	public Type listType = Type.UNKNOWN;
	public NbtSuggestionSubtype subtype = NbtSuggestionSubtype.NONE;
	public @Nullable String subtypeData = null;
	public @Nullable String subtypeWith = null;
	public @Nullable NbtSuggestions subcompound = null;
	public boolean recommended = false;

	public NbtSuggestion(String tag, Type type)
	{
		this.tag = tag;
		this.type = type;
		createdInstanceCounter++;
	}

	public NbtSuggestion(String tag, Type type, Type listType)
	{
		this(tag, type);
		this.listType = listType;
	}

	public NbtSuggestion copy(NbtSuggestions oldParent, NbtSuggestions newParent)
	{
		NbtSuggestion newSuggestion = new NbtSuggestion(tag, type, listType);
		newSuggestion.subtype = subtype;
		newSuggestion.subtypeData = subtypeData;
		newSuggestion.subtypeWith = subtypeWith;
		newSuggestion.recommended = recommended;

		if (subcompound != null)
		{
			if (subcompound == oldParent)
			{
				newSuggestion.subcompound = newParent;
			}
			else
			{
				newSuggestion.subcompound = new NbtSuggestions();
				newSuggestion.subcompound.copyAll(subcompound);
			}
		}
		return newSuggestion;
	}

	public boolean hasSubcompound()
	{
		return type == Type.COMPOUND || listType == Type.COMPOUND;
	}

	public NbtSuggestions getSubcompound()
	{
		if (subcompound == null) { subcompound = new NbtSuggestions(); }
		return subcompound;
	}

	public boolean getSubtypeSuggestions(SuggestionList suggestionList, ParentInfo parentInfo, CustomTagParser.Type parserType)
	{
		return subtype.getSubtypeSuggestions(this, suggestionList, getFinalSubtypeData(parentInfo), parserType);
	}

	public void getSubtypeTagSuggestions(SuggestionList suggestionList, ParentInfo parentInfo, CustomTagParser.Type parserType)
	{
		subtype.getSubtypeTagSuggestions(suggestionList, parentInfo, getFinalSubtypeData(parentInfo), parserType);
	}

	private @Nullable String getFinalSubtypeData(ParentInfo parentInfo)
	{
		if (subtypeWith != null && subtypeData != null && subtypeData.contains("*"))
		{
			if (subtypeWith.equals("#root"))
			{
				if (parentInfo.parentTag == null) { return null; }
				return subtypeData.replace("*", parentInfo.parentTag);
			}
			else if (subtypeWith.equals("#parent/#root"))
			{
				if (parentInfo.secondParentTag == null) { return null; }
				return subtypeData.replace("*", parentInfo.secondParentTag);
			}
			else
			{
				boolean useParentMap = subtypeWith.startsWith("#parent/");
				Map<String, String> map = useParentMap ? parentInfo.parentTagMap : parentInfo.tagMap;
				if (map == null) { return null; }

				String finalSubtypeWith = useParentMap ? subtypeWith.substring(8) : subtypeWith;
				String tagValue = map.get(finalSubtypeWith);

				return tagValue != null ? subtypeData.replace("*", tagValue) : null;
			}
		}
		return subtypeData;
	}

	public String getFinalTagName(ParentInfo parentInfo)
	{
		String finalData = getFinalSubtypeData(parentInfo);
		if (subtype == NbtSuggestionSubtype.TAG && finalData != null)
		{
			return finalData.replace("block/item/", "block/");
		}
		return tag;
	}

	public static NbtSuggestion getDummyCompound(NbtSuggestions subcompound)
	{
		DUMMY_COMPOUND.type = Type.COMPOUND;
		DUMMY_COMPOUND.subcompound = subcompound;
		return DUMMY_COMPOUND;
	}

	public String getSubtext()
	{
		return type.symbol;
	}

	public Message getTooltip()
	{
		return new ComparableLiteralMessage(String.format("%s§r §8%s", tag, type.symbol));
	}

	public void setType(Pair<Type, Type> pair)
	{
		type = pair.getLeft();
		listType = pair.getRight();
	}

	public boolean isAlwaysRelevant()
	{
		//TODO: fix
		return false;
		//return source == Source.ALWAYS_RELEVANT;
	}

	public enum Type
	{
		NOT_FOUND((byte)-1),
		UNKNOWN((byte)-1),
		MULTIPLE((byte)-1),
		BOOLEAN((byte)-1, "b"),
		BYTE(Tag.TAG_BYTE, "b"),
		SHORT(Tag.TAG_SHORT, "s"),
		INT(Tag.TAG_INT),
		LONG(Tag.TAG_LONG, "l"),
		FLOAT(Tag.TAG_FLOAT, "f"),
		DOUBLE(Tag.TAG_DOUBLE),
		STRING(Tag.TAG_STRING),
		LIST(Tag.TAG_LIST),
		BYTE_ARRAY(Tag.TAG_BYTE_ARRAY),
		INT_ARRAY(Tag.TAG_INT_ARRAY),
		LONG_ARRAY(Tag.TAG_LONG_ARRAY),
		COMPOUND(Tag.TAG_COMPOUND),
		UUID((byte)-1);

		private final static Type[] VALUES = values();
		private static final HashMap<String, Type> nameMap = new HashMap<>();
		private static final HashMap<Byte, Type> idMap = new HashMap<>();
		private final byte id;
		private final String lowerCaseName;
		public final String symbol;
		public final String suffix;

		Type(byte id)
		{
			this.id = id;
			this.suffix = "";
			this.lowerCaseName = name().toLowerCase();
			this.symbol = String.format("[%s]", lowerCaseName);
		}

		Type(byte id, String suffix)
		{
			this.id = id;
			this.suffix = suffix;
			this.lowerCaseName = name().toLowerCase();
			this.symbol = String.format("[%s]", lowerCaseName);
		}

		public String getName()
		{
			return lowerCaseName;
		}

		public static void init()
		{
			for (Type type : VALUES)
			{
				nameMap.put(type.getName(), type);
				idMap.put(type.id, type);
			}
		}

		public static Type fromName(String name)
		{
			return nameMap.getOrDefault(name, NOT_FOUND);
		}

		public static Type fromOrdinal(int ordinal)
		{
			return (ordinal < VALUES.length && ordinal >= 0) ? VALUES[ordinal] : UNKNOWN;
		}
	}

	public static class ParentInfo
	{
		private static final ParentInfo BLANK = new ParentInfo(new HashMap<>(), null, null, null);
		public final @Nullable Map<String, String> tagMap;
		public final @Nullable Map<String, String> parentTagMap;
		public @Nullable String parentTag; //TODO: make it final
		public final @Nullable String secondParentTag;

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
			if (BLANK.tagMap != null) { BLANK.tagMap.clear(); }
			if (BLANK.parentTagMap != null) { BLANK.parentTagMap.clear(); }
			BLANK.parentTag = null;
			return BLANK;
		}
	}
}
