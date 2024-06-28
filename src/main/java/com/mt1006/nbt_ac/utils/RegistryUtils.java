package com.mt1006.nbt_ac.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public class RegistryUtils
{
	public static final Registry<? extends Registry<?>> REGISTRY = Registry.REGISTRY;
	public static final LocalRegistry<Item> ITEM = new LocalRegistry<>(Registry.ITEM);
	public static final LocalRegistry<Block> BLOCK = new LocalRegistry<>(Registry.BLOCK);
	public static final LocalRegistry<EntityType<?>> ENTITY_TYPE = new LocalRegistry<>(Registry.ENTITY_TYPE);
	public static final LocalRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new LocalRegistry<>(Registry.BLOCK_ENTITY_TYPE);
	public static final LocalRegistry<Enchantment> ENCHANTMENT = new LocalRegistry<>(Registry.ENCHANTMENT);

	public static class LocalRegistry<T> implements Iterable<T>
	{
		private final Registry<T> registry;

		public LocalRegistry(Registry<T> registry)
		{
			this.registry = registry;
		}

		public @Nullable ResourceLocation getKey(T val)
		{
			return registry.getKey(val);
		}

		public @Nullable T get(ResourceLocation resLoc)
		{
			return registry.get(resLoc);
		}

		public @Nullable T get(String resLoc)
		{
			return registry.get(new ResourceLocation(resLoc));
		}

		public Set<ResourceLocation> keySet()
		{
			return registry.keySet();
		}

		@Override public @NotNull Iterator<T> iterator()
		{
			return registry.iterator();
		}
	}
}
