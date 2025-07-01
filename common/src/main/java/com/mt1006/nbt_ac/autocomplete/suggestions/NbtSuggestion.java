package com.mt1006.nbt_ac.autocomplete.suggestions;

import com.mojang.brigadier.Message;
import com.mt1006.nbt_ac.autocomplete.CustomTagParser;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionMap;
import com.mt1006.nbt_ac.autocomplete.SuggestionList;
import com.mt1006.nbt_ac.autocomplete.type.Type;
import com.mt1006.nbt_ac.utils.ComparableLiteralMessage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class NbtSuggestion
{
	public static int createdInstanceCounter = 0;
	public final String tag;
	public final Type type;
	public @Nullable NbtSuggestionMap subcompound = null;
	private @Nullable Map<Annotation, List<String>> annotations = null;

	public NbtSuggestion(String tag, Type type)
	{
		this.tag = tag;
		this.type = type;
		createdInstanceCounter++;
	}

	public boolean hasSubcompound()
	{
		return subcompound != null;
	}

	public boolean addAnnotation(String name, List<String> args)
	{
		Annotation annotation = Annotation.fromName(name);
		if (annotation == null || (args.isEmpty()) == annotation.argumentsExpected) { return false; }

		if (annotations == null) { annotations = new IdentityHashMap<>(); }
		return (annotations.put(annotation, args) == null);
	}

	public void getSuggestions(SuggestionList suggestionList, CustomTagParser.Type parserType, ParentInfo parentInfo)
	{
		type.getSuggestions(new Type.SuggestionListContext(suggestionList, parserType, parentInfo));
	}

	public void getCompoundSuggestions(SuggestionList suggestionList, CustomTagParser.Type parserType, ParentInfo parentInfo)
	{
		type.getCompoundSuggestions(new Type.SuggestionListContext(suggestionList, parserType, parentInfo));
	}

	/*private @Nullable String getFinalSubtypeData(ParentInfo parentInfo)
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
	}*/

	/*public String getFinalTagName(ParentInfo parentInfo)
	{
		String finalData = getFinalSubtypeData(parentInfo);
		if (subtype == NbtSuggestionSubtype.TAG && finalData != null)
		{
			return finalData.replace("block/item/", "block/");
		}
		return tag;
	}*/

	public String getSubtext()
	{
		return type.getSubtext();
	}

	public Message getTooltip()
	{
		return new ComparableLiteralMessage(String.format("%s§r §8%s", tag, type.getSubtext()));
	}

	public boolean isRelevant()
	{
		//TODO: finish
		return annotations != null && annotations.containsKey(Annotation.ALWAYS_RELEVANT);
	}

	public boolean isRecommended()
	{
		return annotations != null && annotations.containsKey(Annotation.RECOMMENDED);
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
			//newParentInfo.parentTag = suggestion.getFinalTagName(newParentInfo); //TODO: test
			newParentInfo.parentTag = suggestion.tag;
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

	public enum Annotation
	{
		ALWAYS_RELEVANT("AlwaysRelevant", false),
		RELEVANT_IF_EQ("RelevantIfEq", true),
		RELEVANT_IF_DEF("RelevantIfDef", true),
		ARG_SWITCH("ArgSwitch", true),
		RECOMMENDED("Recommended", false);

		private static final Annotation[] VALUES = values();
		private final String name;
		private final boolean argumentsExpected;

		Annotation(String name, boolean argumentExpected)
		{
			this.name = name;
			this.argumentsExpected = argumentExpected;
		}

		public static @Nullable Annotation fromName(String name)
		{
			for (Annotation annotation : VALUES)
			{
				if (annotation.name.equals(name)) { return annotation; }
			}
			return null;
		}
	}
}
