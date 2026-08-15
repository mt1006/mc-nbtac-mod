package net.mt1006.nbtac.autocomplete.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.utils.McVersion;
import net.mt1006.nbtac.utils.RegistryUtils;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DefinedNbtTag extends NbtTag
{
	private @Nullable Map<Annotation, List<String>> annotations = null;

	public DefinedNbtTag(String name, Type type)
	{
		super(name, type);
	}

	@Override public int getPriority(@Nullable ParsedCompound compound)
	{
		return getPriority(isTagRelevant(compound));
	}

	public int getPriority(boolean relevant)
	{
		if (annotations != null)
		{
			List<String> recommendedVal = annotations.get(Annotation.RECOMMENDED);
			if (recommendedVal == null) { recommendedVal = annotations.get(Annotation.OPT_RECOMMENDED); }
			if (recommendedVal != null)
			{
				return 100 + (!recommendedVal.isEmpty() ? Integer.parseInt(recommendedVal.get(0)) : 0);
			}
		}

		if (!relevant) { return -1; }
		return (annotations != null && annotations.containsKey(Annotation.RECOMMENDED_IF_RELEVANT)) ? 100 : 0;
	}

	@Override public @Nullable ResourceLocation getNameAsId()
	{
		// defined NBT tags don't support it, as it isn't necessary
		return null;
	}

	private boolean isTagRelevant(@Nullable ParsedCompound compound)
	{
		if (annotations == null || compound == null) { return true; }
		boolean relevant = true;

		List<String> ifEq = annotations.get(Annotation.RELEVANT_IF_EQ);
		if (ifEq != null && !ifEq.isEmpty())
		{
			String val = compound.getStrVal(ifEq.get(0));
			if (val != null)
			{
				relevant = false;
				for (int i = 1; i < ifEq.size(); i++)
				{
					if (val.equals(ifEq.get(i))) { return true; }
				}
			}
		}

		List<String> ifNotDef = annotations.get(Annotation.RELEVANT_IF_NOT_DEF);
		if (ifNotDef != null)
		{
			for (String tagName : ifNotDef)
			{
				if (compound.containsKey(tagName))
				{
					relevant = false;
					break;
				}
			}
		}

		return relevant;
	}

	public boolean isDataComponentRelevant(@Nullable Item item)
	{
		if (annotations == null) { return false; }
		if (annotations.containsKey(Annotation.ALWAYS_RELEVANT)) { return true; }

		List<String> forItem = annotations.get(Annotation.RELEVANT_COMPONENT_FOR_ITEM);
		if (forItem != null)
		{
			ResourceLocation parentItemId = RegistryUtils.ITEM.getKey(item);
			if (parentItemId != null)
			{
				for (String id : forItem)
				{
					if (parentItemId.equals(ResourceLocation.tryParse(id))) { return true; }
				}
			}
		}

		return false;
	}

	public boolean addAnnotation(String name, List<String> args)
	{
		Annotation annotation = Annotation.fromName(name);
		if (annotation == null) { return false; }

		if (annotations == null) { annotations = new IdentityHashMap<>(); }
		return (annotations.put(annotation, args) == null);
	}

	public boolean inVersionRange()
	{
		if (annotations == null) { return true; }

		List<String> since = annotations.get(Annotation.SINCE);
		if (since != null)
		{
			if (since.size() != 1) { throw new RuntimeException("\"Since\" requires exactly one argument"); }
			if (McVersion.compare(since.get(0)) < 0) { return false; }
		}

		List<String> until = annotations.get(Annotation.UNTIL);
		if (until != null)
		{
			if (until.size() != 1) { throw new RuntimeException("\"Until\" requires exactly one argument"); }
			if (McVersion.compare(until.get(0)) >= 0) { return false; }
		}
		return true;
	}

	public DefinedNbtTag renameIfNecessary()
	{
		if (annotations == null) { return this; }
		List<String> oldName = annotations.get(Annotation.OLD_NAME);
		if (oldName == null || oldName.size() != 2) { return this; }

		if (McVersion.compare(oldName.get(0)) < 0)
		{
			DefinedNbtTag newTag = new DefinedNbtTag(oldName.get(1), getType());
			newTag.annotations = annotations;
			return newTag;
		}
		return this;
	}

	public enum Annotation
	{
		// only for NBT tags (including in item data component compounds)
		RELEVANT_IF_EQ("RelevantIfEq"),
		RELEVANT_IF_NOT_DEF("RelevantIfNotDef"),

		// only for item data components
		ALWAYS_RELEVANT("AlwaysRelevant"),
		RELEVANT_COMPONENT_FOR_ITEM("RelevantComponentForItem"),

		// universal
		RECOMMENDED("Recommended"),
		OPT_RECOMMENDED("OptRecommended"),
		RECOMMENDED_IF_RELEVANT("RecommendedIfRelevant"),
		SINCE("Since"),
		UNTIL("Until"),
		OLD_NAME("OldName");

		private static final Annotation[] VALUES = values();
		private final String name;

		Annotation(String name)
		{
			this.name = name;
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
