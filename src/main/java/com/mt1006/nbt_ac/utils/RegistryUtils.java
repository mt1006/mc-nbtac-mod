package com.mt1006.nbt_ac.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public class RegistryUtils
{
	public static final Registry<? extends Registry<?>> REGISTRY = Registry.REGISTRY;
	public static final LocalRegistry<Item> ITEM = new LocalRegistry<>(ForgeRegistries.ITEMS);
	public static final LocalRegistry<Block> BLOCK = new LocalRegistry<>(ForgeRegistries.BLOCKS);
	public static final LocalRegistry<EntityType<?>> ENTITY_TYPE = new LocalRegistry<>(ForgeRegistries.ENTITY_TYPES);
	public static final LocalRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new LocalRegistry<>(ForgeRegistries.BLOCK_ENTITY_TYPES);
	public static final LocalRegistry<Enchantment> ENCHANTMENT = new LocalRegistry<>(ForgeRegistries.ENCHANTMENTS);

	public static class LocalRegistry<T> implements Iterable<T>
	{
		private final IForgeRegistry<T> registry;

		public LocalRegistry(IForgeRegistry<T> registry)
		{
			this.registry = registry;
		}

		public @Nullable ResourceLocation getKey(T val)
		{
			return registry.getKey(val);
		}

		public @Nullable T get(ResourceLocation resLoc)
		{
			return registry.getValue(resLoc);
		}

		public @Nullable T get(String resLoc)
		{
			return registry.getValue(new ResourceLocation(resLoc));
		}

		public Set<ResourceLocation> keySet()
		{
			return registry.getKeys();
		}

		@Override public @NotNull Iterator<T> iterator()
		{
			return registry.iterator();
		}
	}
}
