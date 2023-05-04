package com.mt1006.nbt_ac.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class RegistryUtils
{
	public static final Registry<? extends Registry<?>> REGISTRY = Registry.REGISTRY;
	public static final LocalRegistry<Item> ITEM = new LocalRegistry<>(ForgeRegistries.ITEMS);
	public static final LocalRegistry<Block> BLOCK = new LocalRegistry<>(ForgeRegistries.BLOCKS);
	public static final LocalRegistry<EntityType<?>> ENTITY_TYPE = new LocalRegistry<>(ForgeRegistries.ENTITIES);
	public static final LocalRegistry<TileEntityType<?>> BLOCK_ENTITY_TYPE = new LocalRegistry<>(ForgeRegistries.TILE_ENTITIES);

	public static class LocalRegistry<T extends IForgeRegistryEntry<T>> implements Iterable<T>
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
