package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class TypeLoader
{
	public static volatile @Nullable Thread objectCatcher = null;
	public static Object lastObject = null;

	public static void loadEntityTypes()
	{
		objectCatcher = Thread.currentThread();

		for (EntityType<?> entityType : RegistryUtils.ENTITY_TYPE)
		{
			lastObject = null;
			Class<?> clazz;
			ResourceLocation resourceName = EntityType.getKey(entityType);

			if (resourceName.toString().equals("minecraft:player"))
			{
				clazz = ServerPlayer.class;
			}
			else
			{
				try
				{
					entityType.create(null); // lastObject set by mixin (constructors.EntityMixin)
				}
				catch (Throwable throwable)
				{
					if (throwable instanceof Error)
					{
						NBTac.LOGGER.error("Entity \"{}\" constructor thrown error: {}", resourceName, throwable);
					}
				}
				clazz = lastObject != null ? lastObject.getClass() : null;
			}

			if (clazz != null)
			{
				try
				{
					NbtSuggestions suggestions = new NbtSuggestions(true);
					Disassembly.disassemblyEntity(clazz, suggestions);
					NbtSuggestionManager.add("entity/" + resourceName, suggestions);
				}
				catch (Exception exception)
				{
					NBTac.LOGGER.error("Failed to load entity \"{}\": {}", resourceName, exception);
					Loader.printStackTrace(exception);
				}
			}
			else
			{
				NBTac.LOGGER.error("Unable to get entity class for \"{}\"", resourceName);
			}
		}

		objectCatcher = null;
	}

	public static void loadBlockEntityTypes()
	{
		objectCatcher = Thread.currentThread();

		for (BlockEntityType<?> blockEntityType : RegistryUtils.BLOCK_ENTITY_TYPE)
		{
			lastObject = null;
			Class<?> clazz;
			ResourceLocation resourceName = BlockEntityType.getKey(blockEntityType);

			try
			{
				blockEntityType.create(BlockPos.ZERO, null); // lastObject set by mixin (constructors.BlockEntityMixin)
			}
			catch (Throwable throwable)
			{
				if (throwable instanceof Error)
				{
					NBTac.LOGGER.error("Block entity \"{}\" constructor thrown error: {}", resourceName, throwable);
				}
			}
			clazz = lastObject != null ? lastObject.getClass() : null;

			if (clazz != null)
			{
				try
				{
					NbtSuggestions suggestions = new NbtSuggestions(true);
					Disassembly.disassemblyBlockEntity(clazz, suggestions);
					NbtSuggestionManager.add("block/" + resourceName, suggestions);
				}
				catch (Exception exception)
				{
					NBTac.LOGGER.error("Failed to load block entity \"{}\": {}", resourceName, exception);
					Loader.printStackTrace(exception);
				}
			}
			else
			{
				NBTac.LOGGER.error("Unable to get block entity class for \"{}\"", resourceName);
			}
		}

		objectCatcher = null;
	}

	public static @Nullable BlockEntity blockEntityFromBlock(Block block)
	{
		if (!ModConfig.allowBlockEntityExtraction.val) { return null; }
		ResourceLocation resLoc = RegistryUtils.BLOCK.getKey(block);
		if (resLoc == null) { return null; }

		BlockEntityType<?> blockEntityType = RegistryUtils.BLOCK_ENTITY_TYPE.get(resLoc);
		if (blockEntityType == null) { return null; }

		if (objectCatcher == Loader.getLoaderThread()) { return null; }
		objectCatcher = Thread.currentThread();
		lastObject = null;

		try
		{
			blockEntityType.create(BlockPos.ZERO, null);
		}
		catch (Throwable throwable)
		{
			if (throwable instanceof Error)
			{
				NBTac.LOGGER.error("Block entity \"{}\" constructor thrown error: {}", resLoc, throwable);
			}
		}

		objectCatcher = null;
		return (lastObject instanceof BlockEntity) ? (BlockEntity)lastObject : null;
	}
}
