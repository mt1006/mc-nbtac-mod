package com.mt1006.nbt_ac.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RegistryUtils
{
	public static final Registry<? extends Registry<?>> REGISTRY = BuiltInRegistries.REGISTRY;
	public static final LocalRegistry<Item> ITEM = new VanillaLocalRegistry<>(BuiltInRegistries.ITEM);
	public static final LocalRegistry<Block> BLOCK = new VanillaLocalRegistry<>(BuiltInRegistries.BLOCK);
	public static final LocalRegistry<EntityType<?>> ENTITY_TYPE = new VanillaLocalRegistry<>(BuiltInRegistries.ENTITY_TYPE);
	public static final LocalRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new VanillaLocalRegistry<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
	public static final LocalRegistry<DataComponentType<?>> DATA_COMPONENT_TYPE = new VanillaLocalRegistry<>(BuiltInRegistries.DATA_COMPONENT_TYPE);

	public static class VanillaLocalRegistry<T> implements LocalRegistry<T>
	{
		private final Registry<T> registry;

		public VanillaLocalRegistry(Registry<T> registry)
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
			return registry.get(ResourceLocation.parse(resLoc));
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

	public interface LocalRegistry<T> extends Iterable<T>
	{
		@Nullable ResourceLocation getKey(T val);
		@Nullable T get(ResourceLocation resLoc);
		@Nullable T get(String resLoc);
		Set<ResourceLocation> keySet();
		Set<Map.Entry<ResourceKey<T>, T>> entrySet();
		@Override @NotNull Iterator<T> iterator();
	}
}
