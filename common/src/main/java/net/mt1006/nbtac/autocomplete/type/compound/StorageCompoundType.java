package net.mt1006.nbtac.autocomplete.type.compound;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.parser.ParsedCompound;
import net.mt1006.nbtac.autocomplete.tag.GeneratedNbtTag;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;
import net.mt1006.nbtac.autocomplete.type.Type;
import org.jetbrains.annotations.Nullable;

public class StorageCompoundType extends ComplexCompoundType
{
	private final @Nullable Identifier id;

	public StorageCompoundType(@Nullable Identifier id)
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
			compound.entrySet().forEach((e) -> map.add(new GeneratedNbtTag(e.getKey(), typeFromTag(e.getValue()))));
		}

		private static Type typeFromTag(Tag tag)
		{
			return switch (tag)
			{
				case CompoundTag compoundTag -> new DynamicCompoundType(compoundTag);
				case ByteTag ignore -> PrimitiveType.BYTE;
				case ShortTag ignore -> PrimitiveType.SHORT;
				case IntTag ignore -> PrimitiveType.INT;
				case LongTag ignore -> PrimitiveType.LONG;
				case FloatTag ignore -> PrimitiveType.FLOAT;
				case DoubleTag ignore -> PrimitiveType.DOUBLE;
				case ByteArrayTag ignore -> PrimitiveType.BYTE_ARRAY;
				case IntArrayTag ignore -> PrimitiveType.INT_ARRAY;
				case LongArrayTag ignore -> PrimitiveType.LONG_ARRAY;
				case StringTag ignore -> PrimitiveType.STRING;
				case ListTag ignore -> PrimitiveType.LIST; //TODO: support compounds in lists
				case EndTag ignore -> PrimitiveType.UNKNOWN;
			};
		}
	}
}
