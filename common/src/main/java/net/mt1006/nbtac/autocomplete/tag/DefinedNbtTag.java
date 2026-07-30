package net.mt1006.nbtac.autocomplete.tag;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.type.Type;
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
		if (annotations != null)
		{
			List<String> recommendedVal = annotations.get(Annotation.RECOMMENDED);
			if (recommendedVal == null) { recommendedVal = annotations.get(Annotation.OPT_RECOMMENDED); }
			if (recommendedVal != null)
			{
				return 100 + (!recommendedVal.isEmpty() ? Integer.parseInt(recommendedVal.getFirst()) : 0);
			}
		}

		return isRelevant(true, compound) ? 0 : -1;
	}

	@Override public @Nullable Identifier getNameAsId()
	{
		// defined NBT tags don't support it, as it isn't necessary
		return null;
	}

	public boolean isRelevant(boolean relevantByDefault, @Nullable ParsedCompound compound)
	{
		if (!relevantByDefault)
		{
			return annotations != null && annotations.containsKey(Annotation.ALWAYS_RELEVANT);
		}
		else
		{
			if (annotations == null || compound == null) { return true; }
			boolean isRelevant = true;

			List<String> ifEq = annotations.get(Annotation.RELEVANT_IF_EQ);
			if (ifEq != null && !ifEq.isEmpty())
			{
				String val = compound.getStrVal(ifEq.getFirst());
				if (val != null)
				{
					isRelevant = false;
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
						isRelevant = false;
						break;
					}
				}
			}

			return isRelevant;
		}
	}

	public boolean addAnnotation(String name, List<String> args)
	{
		Annotation annotation = Annotation.fromName(name);
		if (annotation == null) { return false; }

		if (annotations == null) { annotations = new IdentityHashMap<>(); }
		return (annotations.put(annotation, args) == null);
	}

	public enum Annotation
	{
		ALWAYS_RELEVANT("AlwaysRelevant"),
		RELEVANT_IF_EQ("RelevantIfEq"),
		RELEVANT_IF_NOT_DEF("RelevantIfNotDef"),
		RECOMMENDED("Recommended"),
		OPT_RECOMMENDED("OptRecommended");

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
