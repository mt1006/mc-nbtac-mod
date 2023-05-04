package com.mt1006.nbt_ac.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class RegistryUtils
{
	public static final Registry<? extends Registry<?>> REGISTRY = Registry.REGISTRY;
	public static final LocalRegistry<Item> ITEM = new LocalRegistry<>(ForgeRegistries.ITEMS);
	public static final LocalRegistry<Block> BLOCK = new LocalRegistry<>(ForgeRegistries.BLOCKS);
	public static final LocalRegistry<EntityType<?>> ENTITY_TYPE = new LocalRegistry<>(ForgeRegistries.ENTITY_TYPES);
	public static final LocalRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new LocalRegistry<>(ForgeRegistries.BLOCK_ENTITY_TYPES);

	public static class LocalRegistry<T> implements Iterable<T>
	{
		private final IForgeRegistry<T> registry;

		public LocalRegistry(IForgeRegistry<T> registry)
		{
			this.registry = registry;
		}

		public ResourceLocation getKey(T val)
		{
			return registry.getKey(val);
		}

		public T get(ResourceLocation resLoc)
		{
			return registry.getValue(resLoc);
		}

		public T get(String resLoc)
		{
			return registry.getValue(new ResourceLocation(resLoc));
		}

		@Override public @NotNull Iterator<T> iterator()
		{
			return registry.iterator();
		}
	}
}
