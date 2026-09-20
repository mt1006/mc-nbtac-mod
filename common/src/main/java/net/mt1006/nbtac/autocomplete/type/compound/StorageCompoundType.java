package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.mixin.fields.CompoundTagFields;
import org.jetbrains.annotations.Nullable;

public class StorageCompoundType extends ComplexCompoundType
{
	private final @Nullable ResourceLocation id;

	public StorageCompoundType(@Nullable ResourceLocation id)
	{
		this.id = id;
	}

	@Override protected void getBasicCompoundSuggestions(@Nullable ParsedCompound parsed, NbtTagMap map)
	{
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (id == null || server == null) { return; }

		new DynamicCompoundType(server.getCommandStorage().get(id)).getBasicCompoundSuggestions(parsed, map);
	}

	private static class DynamicCompoundType extends ComplexCompoundType
	{
		private final CompoundTag compound;

		public DynamicCompoundType(CompoundTag compound)
		{
			this.compound = compound;
		}

		@Override protected void getBasicCompoundSuggestions(@Nullable ParsedCompound parsed, NbtTagMap map)
		{
			((CompoundTagFields)compound).nbtac$getTags().forEach((k, v) -> map.add(new GeneratedNbtTag(k, typeFromTag(v))));
		}

		private static Type typeFromTag(Tag tag)
		{
			if (tag instanceof CompoundTag compoundTag) { return new DynamicCompoundType(compoundTag); }
			if (tag instanceof ByteTag) { return PrimitiveType.BYTE; }
			if (tag instanceof ShortTag) { return PrimitiveType.SHORT; }
			if (tag instanceof IntTag) { return PrimitiveType.INT; }
			if (tag instanceof LongTag) { return PrimitiveType.LONG; }
			if (tag instanceof FloatTag) { return PrimitiveType.FLOAT; }
			if (tag instanceof DoubleTag) { return PrimitiveType.DOUBLE; }
			if (tag instanceof ByteArrayTag) { return PrimitiveType.BYTE_ARRAY; }
			if (tag instanceof IntArrayTag) { return PrimitiveType.INT_ARRAY; }
			if (tag instanceof LongArrayTag) { return PrimitiveType.LONG_ARRAY; }
			if (tag instanceof StringTag) { return PrimitiveType.STRING; }
			if (tag instanceof ListTag) { return PrimitiveType.LIST; }
			return PrimitiveType.UNKNOWN;
		}
	}
}
