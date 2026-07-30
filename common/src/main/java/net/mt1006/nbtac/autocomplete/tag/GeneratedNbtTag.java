package net.mt1006.nbtac.autocomplete.tag;

import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class GeneratedNbtTag extends NbtTag
{
	private final int priority;
	private final @Nullable Identifier nameAsId;
	private Function<String, String> subtext = Function.identity();

	public GeneratedNbtTag(String name, Type type)
	{
		this(name, type, 0, null);
	}

	public GeneratedNbtTag(NbtTag toCopy, int priority, @Nullable Identifier nameAsId)
	{
		this(toCopy.getName(), toCopy.getType(), priority, nameAsId);
	}

	public GeneratedNbtTag(String name, Type type, int priority, @Nullable Identifier nameAsId)
	{
		super(name, type);
		this.priority = priority;
		this.nameAsId = nameAsId;
	}

	public GeneratedNbtTag withSubtext(Function<String, String> subtext)
	{
		this.subtext = subtext;
		return this;
	}

	@Override public String getSubtext()
	{
		return subtext.apply(super.getSubtext());
	}

	@Override public int getPriority(@Nullable ParsedCompound compound)
	{
		return priority;
	}

	@Override public @Nullable Identifier getNameAsId()
	{
		return nameAsId;
	}
}
