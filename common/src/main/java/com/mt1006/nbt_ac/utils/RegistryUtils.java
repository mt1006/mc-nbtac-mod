package com.mt1006.nbt_ac.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RegistryUtils
{
	public static final Registry<? extends Registry<?>> REGISTRY = BuiltInRegistries.REGISTRY;
	public static final LocalRegistry<Item> ITEM = new LocalRegistry<>(BuiltInRegistries.ITEM);
	public static final LocalRegistry<Block> BLOCK = new LocalRegistry<>(BuiltInRegistries.BLOCK);
	public static final LocalRegistry<EntityType<?>> ENTITY_TYPE = new LocalRegistry<>(BuiltInRegistries.ENTITY_TYPE);
	public static final LocalRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new LocalRegistry<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
	public static final LocalRegistry<DataComponentType<?>> DATA_COMPONENT_TYPE = new LocalRegistry<>(BuiltInRegistries.DATA_COMPONENT_TYPE);

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
			Holder.Reference<T> ref = registry.get(resLoc).orElse(null);
			return ref != null ? ref.value() : null;
		}

		public @Nullable T get(String resLoc)
		{
			return get(ResourceLocation.parse(resLoc));
		}

		public Set<ResourceLocation> keySet()
		{
			return registry.keySet();
		}

		public Set<Map.Entry<ResourceKey<T>, T>> entrySet()
		{
			return registry.entrySet();
		}

		@Override public @NotNull Iterator<T> iterator()
		{
			return registry.iterator();
		}
	}
}
