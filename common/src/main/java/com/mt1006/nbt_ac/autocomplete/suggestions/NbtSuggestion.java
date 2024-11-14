package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.SuggestionList;
import com.mt1006.nbt_ac.autocomplete.loader.typeloader.Disassembly;
import com.mt1006.nbt_ac.utils.ComparableLiteralMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;

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
	public Source source = Source.DEFAULT;
	public boolean recommended = false;

	public NbtSuggestion(String tag, Type type)
	{
		this.tag = tag;
		this.type = type;
		createdInstanceCounter++;
	}

	public NbtSuggestion(String tag, Type type, Source source)
	{
		this(tag, type);
		this.source = source;
	}

	public NbtSuggestion(String tag, Type type, Source source, Type listType)
	{
		this(tag, type, source);
		this.listType = listType;
	}

	public NbtSuggestion copy(boolean prediction, NbtSuggestions oldParent, NbtSuggestions newParent)
	{
		NbtSuggestion newSuggestion = new NbtSuggestion(tag, type, source, listType);
		newSuggestion.subtype = subtype;
		newSuggestion.subtypeData = subtypeData;
		newSuggestion.subtypeWith = subtypeWith;
		newSuggestion.recommended = recommended;

		if (prediction) {newSuggestion.changeSuggestionSource(Source.PREDICTION); }

		if (subcompound != null)
		{
			if (subcompound == oldParent)
			{
				newSuggestion.subcompound = newParent;
			}
			else
			{
				newSuggestion.subcompound = new NbtSuggestions(true);
				newSuggestion.subcompound.copyAll(subcompound, prediction);
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
		if (subcompound == null) { subcompound = new NbtSuggestions(true); }
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

	public void changeSuggestionSource(Source newSource)
	{
		if (newSource.level >= source.level) { source = newSource; }
	}

	public static NbtSuggestion getDummyCompound(NbtSuggestions subcompound)
	{
		DUMMY_COMPOUND.type = Type.COMPOUND;
		DUMMY_COMPOUND.subcompound = subcompound;
		return DUMMY_COMPOUND;
	}

	public String getSubtext()
	{
		return source.symbol + type.symbol;
	}

	public Message getTooltip()
	{
		return new ComparableLiteralMessage(String.format("%s§r §8%s%s", tag, source.name, type.symbol));
	}

	public void setType(Pair<Type, Type> pair)
	{
		type = pair.getLeft();
		listType = pair.getRight();
	}

	//TODO: do something about "always relevant" being implemented in such a way
	public void setAlwaysRelevant()
	{
		source = Source.ALWAYS_RELEVANT;
	}

	public boolean isAlwaysRelevant()
	{
		return source == Source.ALWAYS_RELEVANT;
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
		private static final HashMap<String, Type> methodNameMap = new HashMap<>();
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

			try
			{
				ClassNode classNode = Disassembly.loadClass(CompoundTag.class.getCanonicalName(), null);
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

		public static Type fromOrdinal(int ordinal)
		{
			return (ordinal < VALUES.length && ordinal >= 0) ? VALUES[ordinal] : UNKNOWN;
		}
	}

	public enum Source
	{
		//TODO: remove compound prediction?
		DEFAULT("", "", 0),
		ALWAYS_RELEVANT("", "", 0), // used to mark item components as always relevant
		UNCERTAIN("(?) ", "(uncertain) ", 1),
		COMPOUND_PREDICTION("(C) ", "(compound prediction) ", 2),
		SUBTYPE_PREDICTION("(S) ", "(subtype prediction) ", 3),
		TYPE_PREDICTION("(T) ", "(type prediction) ", 4),
		PREDICTION("(P) ", "(prediction) ", 5);

		private final static Source[] VALUES = values();
		public final String symbol;
		public final String name;
		public final int level;

		Source(String symbol, String name, int level)
		{
			this.symbol = symbol;
			this.name = name;
			this.level = level;
		}

		public static Source fromOrdinal(int ordinal)
		{
			return (ordinal < VALUES.length && ordinal >= 0) ? VALUES[ordinal] : DEFAULT;
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
