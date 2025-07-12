package net.mt1006.nbtac.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.config.ModConfig;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCatcher
{
	public static @Nullable Thread thread = null;
	public static @Nullable Object lastObject = null;

	public static @Nullable Class<?> catchFromBlock(Block block)
	{
		if (!ModConfig.allowBlockEntityExtraction.val) { return null; }
		ResourceLocation resLoc = RegistryUtils.BLOCK.getKey(block);
		if (resLoc == null) { return null; }

		BlockEntityType<?> blockEntityType = RegistryUtils.BLOCK_ENTITY_TYPE.get(resLoc);
		if (blockEntityType == null) { return null; }

		if (thread != null && thread != Thread.currentThread()) { return null; }
		thread = Thread.currentThread();
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

		thread = null;
		return lastObject != null ? lastObject.getClass() : null;
	}
}
