package net.mt1006.nbtac.autocomplete.tag;

import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

public class GeneratedNbtTag extends NbtTag
{
	private final int priority;
	private final @Nullable ResourceLocation nameAsId;
	private @Nullable String subtext;

	public GeneratedNbtTag(String name, Type type)
	{
		this(name, type, 0, null);
	}

	public GeneratedNbtTag(NbtTag toCopy, int priority)
	{
		this(toCopy.getName(), toCopy.getType(), priority, null);
	}

	public GeneratedNbtTag(String name, Type type, int priority, @Nullable ResourceLocation nameAsId)
	{
		super(name, type);
		this.priority = priority;
		this.nameAsId = nameAsId;
	}

	public GeneratedNbtTag withSubtext(String subtext)
	{
		this.subtext = subtext;
		return this;
	}

	@Override public String getSubtext()
	{
		return subtext != null ? subtext : super.getSubtext();
	}

	@Override public int getPriority(@Nullable ParsedCompound compound)
	{
		return priority;
	}

	@Override public @Nullable ResourceLocation getNameAsId()
	{
		return nameAsId;
	}
}
