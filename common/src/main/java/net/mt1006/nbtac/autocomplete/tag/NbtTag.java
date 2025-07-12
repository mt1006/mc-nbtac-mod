package net.mt1006.nbtac.autocomplete.tag;

import com.mojang.brigadier.Message;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.utils.ComparableLiteralMessage;
import org.jetbrains.annotations.Nullable;

public abstract class NbtTag
{
	private final String name;
	private final Type type;

	public NbtTag(String name, Type type)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public Type getType()
	{
		return type;
	}

	public String getSubtext()
	{
		return type.getSubtext();
	}

	public Message getTooltip()
	{
		return new ComparableLiteralMessage(String.format("%s§r §8%s", name, getSubtext()));
	}

	public abstract int getPriority(@Nullable ParsedCompound compound);

	public abstract @Nullable ResourceLocation getNameAsId();
}
