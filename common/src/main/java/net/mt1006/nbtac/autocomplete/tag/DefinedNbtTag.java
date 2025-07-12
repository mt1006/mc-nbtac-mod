package net.mt1006.nbtac.autocomplete.tag;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.parser.ParsedPrimitive;
import net.mt1006.nbtac.autocomplete.parser.ParsedTag;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DefinedNbtTag extends NbtTag
{
	public static int instanceCounter = 0; //TODO: remove?
	private @Nullable Map<Annotation, List<String>> annotations = null;

	public DefinedNbtTag(String name, Type type)
	{
		super(name, type);
		instanceCounter++;
	}

	@Override public int getPriority(@Nullable ParsedCompound compound)
	{
		if (isRecommended()) { return 100; }
		return isRelevant(true, compound) ? 0 : -1;
	}

	@Override public @Nullable ResourceLocation getNameAsId()
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
				ParsedTag tag = compound.get(ifEq.getFirst());
				if (tag != null && tag.val instanceof ParsedPrimitive tagVal && tagVal.val != null)
				{
					isRelevant = false;
					for (int i = 1; i < ifEq.size(); i++)
					{
						if (tagVal.val.equals(ifEq.get(i))) { return true; }
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

	public boolean isRecommended()
	{
		return annotations != null && annotations.containsKey(Annotation.RECOMMENDED);
	}

	public boolean addAnnotation(String name, List<String> args)
	{
		Annotation annotation = Annotation.fromName(name);
		if (annotation == null || (args.isEmpty()) == annotation.argumentsExpected) { return false; }

		if (annotations == null) { annotations = new IdentityHashMap<>(); }
		return (annotations.put(annotation, args) == null);
	}

	public enum Annotation
	{
		ALWAYS_RELEVANT("AlwaysRelevant", false),
		RELEVANT_IF_EQ("RelevantIfEq", true),
		RELEVANT_IF_NOT_DEF("RelevantIfNotDef", true),
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
