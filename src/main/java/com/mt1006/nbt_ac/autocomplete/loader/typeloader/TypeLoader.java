package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.utils.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TypeLoader
{
	public static volatile boolean getClasses = false;
	public static Class<?> lastClass = null;

	public static void loadEntityTypes()
	{
		getClasses = true;

		for (EntityType<?> entityType : RegistryUtils.ENTITY_TYPE)
		{
			lastClass = null;
			ResourceLocation resourceName = EntityType.getKey(entityType);

			long start = System.currentTimeMillis();
			if (resourceName.toString().equals("minecraft:player"))
			{
				lastClass = Player.class;
			}
			else
			{
				try
				{
					entityType.create(null); // lastClass set by mixin (constructors.EntityMixin)
				}
				catch (Throwable throwable)
				{
					if (throwable instanceof Error)
					{
						NBTac.LOGGER.error("Entity \"" + resourceName + "\" constructor thrown error: " + throwable);
					}
				}
			}

			if (lastClass != null)
			{
				try
				{
					NbtSuggestions suggestions = new NbtSuggestions();
					Disassembly.disassemblyEntity(lastClass, suggestions);
					NbtSuggestionManager.add("entity/" + resourceName, suggestions);
				}
				catch (Exception exception)
				{
					NBTac.LOGGER.error("Failed to load entity \"" + resourceName + "\": " + exception);
					Loader.printStackTrace(exception);
				}
			}
			else
			{
				NBTac.LOGGER.error("Unable to get entity class for \"" + resourceName + "\"");
			}
		}

		getClasses = false;
	}

	public static void loadBlockEntityTypes()
	{
		getClasses = true;

		for (BlockEntityType<?> blockEntityType : RegistryUtils.BLOCK_ENTITY_TYPE)
		{
			lastClass = null;
			ResourceLocation resourceName = BlockEntityType.getKey(blockEntityType);

			try
			{
				blockEntityType.create(BlockPos.ZERO, null);
			}
			catch (Throwable throwable)
			{
				if (throwable instanceof Error)
				{
					NBTac.LOGGER.error("Block entity \"" + resourceName + "\" constructor thrown error: " + throwable);
				}
			}

			if (lastClass != null) // set by mixin (constructors.BlockEntityMixin)
			{
				try
				{
					NbtSuggestions suggestions = new NbtSuggestions();
					Disassembly.disassemblyBlockEntity(lastClass, suggestions);
					NbtSuggestionManager.add("block/" + resourceName, suggestions);
				}
				catch (Exception exception)
				{
					NBTac.LOGGER.error("Failed to load block entity \"" + resourceName + "\": " + exception);
					Loader.printStackTrace(exception);
				}
			}
			else
			{
				NBTac.LOGGER.error("Unable to get block entity class for \"" + resourceName + "\"");
			}
		}

		getClasses = false;
	}
}
