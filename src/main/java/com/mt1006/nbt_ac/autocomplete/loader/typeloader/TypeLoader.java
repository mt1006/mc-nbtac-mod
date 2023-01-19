package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class TypeLoader
{
	public static boolean getClasses = false;
	public static Class<?> lastClass = null;

	public static void loadEntityTypes()
	{
		getClasses = true;

		for (EntityType<?> entityType : Registry.ENTITY_TYPE)
		{
			lastClass = null;
			ResourceLocation resourceName = EntityType.getKey(entityType);

			if (resourceName.toString().equals("minecraft:player"))
			{
				lastClass = PlayerEntity.class;
			}
			else
			{
				try
				{
					entityType.create(null); // lastClass set by mixin (common.EntityMixin)
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
					exception.printStackTrace();
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

		for (TileEntityType<?> blockEntityType : Registry.BLOCK_ENTITY_TYPE)
		{
			lastClass = null;
			ResourceLocation resourceName = TileEntityType.getKey(blockEntityType);

			try
			{
				blockEntityType.create();
			}
			catch (Throwable throwable)
			{
				if (throwable instanceof Error)
				{
					NBTac.LOGGER.error("Block entity \"" + resourceName + "\" constructor thrown error: " + throwable);
				}
			}

			if (lastClass != null) // set by mixin (common.BlockEntityMixin)
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
					exception.printStackTrace();
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
