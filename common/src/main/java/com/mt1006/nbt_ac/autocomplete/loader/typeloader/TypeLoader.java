package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import com.mt1006.nbt_ac.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
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
			Identifier id = EntityType.getKey(entityType);

			if (id.toString().equals("minecraft:player"))
			{
				clazz = ServerPlayer.class;
			}
			else
			{
				try
				{
					entityType.create(null, EntitySpawnReason.COMMAND); // lastObject set by mixin (constructors.EntityMixin)
				}
				catch (Throwable throwable)
				{
					if (throwable instanceof Error)
					{
						NBTac.LOGGER.error("Entity \"{}\" constructor thrown error: {}", id, throwable);
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
					NbtSuggestionManager.add("entity/" + id, suggestions);
				}
				catch (Exception e)
				{
					NBTac.LOGGER.error("Failed to load entity \"{}\": {}", id, e);
					Loader.printStackTrace(e);
				}
			}
			else
			{
				NBTac.LOGGER.error("Unable to get entity class for \"{}\"", id);
			}
		}

		objectCatcher = null;
	}

	public static void loadBlockEntityTypes()
	{
		objectCatcher = Thread.currentThread();

		//https://github.com/mt1006/mc-nbtac-mod/issues/18
		boolean pistonCrashFix = NBTac.loaderInterface.isFabric() && Utils.isModPresent("moreculling")
				&& Utils.isModPresent("modernfix") && Utils.isModPresent("lithium");

		for (BlockEntityType<?> blockEntityType : RegistryUtils.BLOCK_ENTITY_TYPE)
		{
			//TODO: clean up code
			lastObject = null;
			Class<?> clazz;
			Identifier id = BlockEntityType.getKey(blockEntityType);

			if (pistonCrashFix && blockEntityType == BlockEntityType.PISTON)
			{
				clazz = PistonMovingBlockEntity.class;
			}
			else
			{
				try
				{
					blockEntityType.create(BlockPos.ZERO, null); // lastObject set by mixin (constructors.BlockEntityMixin)
				}
				catch (Throwable throwable)
				{
					if (throwable instanceof Error)
					{
						NBTac.LOGGER.error("Block entity \"{}\" constructor thrown error: {}", id, throwable);
					}
					//TODO: improve error logging (add stacktrace), add logs for non-error throwable when lastObject==null
				}
				clazz = lastObject != null ? lastObject.getClass() : null;
			}

			if (clazz != null)
			{
				try
				{
					NbtSuggestions suggestions = new NbtSuggestions(true);
					Disassembly.disassemblyBlockEntity(clazz, suggestions);
					NbtSuggestionManager.add("block/" + id, suggestions);
				}
				catch (Exception e)
				{
					NBTac.LOGGER.error("Failed to load block entity \"{}\": {}", id, e);
					Loader.printStackTrace(e);
				}
			}
			else
			{
				NBTac.LOGGER.error("Unable to get block entity class for \"{}\"", id);
			}
		}

		objectCatcher = null;
	}

	public static @Nullable BlockEntity blockEntityFromBlock(Block block)
	{
		if (!ModConfig.allowBlockEntityExtraction.val) { return null; }
		Identifier id = RegistryUtils.BLOCK.getKey(block);
		if (id == null) { return null; }

		BlockEntityType<?> blockEntityType = RegistryUtils.BLOCK_ENTITY_TYPE.get(id);
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
				NBTac.LOGGER.error("Block entity \"{}\" constructor thrown error: {}", id, throwable);
			}
		}

		objectCatcher = null;
		return (lastObject instanceof BlockEntity) ? (BlockEntity)lastObject : null;
	}
}
