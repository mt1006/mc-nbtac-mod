package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.LocationInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.UUID;

public class MixinUtils
{
	private static final CommandSource DUMMY_COMMAND_SOURCE_STACK =
			new CommandSource(null, Vector3d.ZERO, Vector2f.ZERO, null, 0, null, null, null, null);

	public static String getNodeString(CommandContext<?> commandContext, int pos)
	{
		return commandContext.getNodes().get(pos).getNode().getName();
	}

	public static String getArgumentString(CommandContext<?> commandContext, String argumentName)
	{
		Map<String, ParsedArgument<?, ?>> arguments;

		try { arguments = (Map<String, ParsedArgument<?, ?>>)Fields.commandContextArguments.get(commandContext); }
		catch (Exception exception) { return null; }

		ParsedArgument<?, ?> argument = arguments.get(argumentName);
		if (argument == null) { return null; }

		return argument.getRange().get(commandContext.getInput());
	}

	public static String blockFromCoords(ILocationArgument coords)
	{
		if (!(coords instanceof LocationInput)) { return null; }
		if (coords.isXRelative() || coords.isYRelative() || coords.isZRelative()) { return null; }
		BlockPos blockPos = coords.getBlockPos(DUMMY_COMMAND_SOURCE_STACK);

		ClientWorld level = Minecraft.getInstance().level;
		if (level == null) { return null; }
		Block block = level.getBlockState(blockPos).getBlock();

		return "block/" + Registry.BLOCK.getKey(block);
	}

	public static String entityFromEntitySelector(EntitySelector entitySelector)
	{
		try
		{
			Object typeObject = Fields.entitySelectorType.get(entitySelector);
			Object uuidObject = Fields.entitySelectorUUID.get(entitySelector);
			Object playerNameObject = Fields.entitySelectorPlayerName.get(entitySelector);
			return entityFromSelectorData(typeObject, uuidObject, playerNameObject);
		}
		catch (Exception exception) { return null; }
	}

	public static String entityFromSelectorData(Object typeObject, Object uuidObject, Object playerNameObject)
	{
		if (typeObject instanceof EntityType)
		{
			EntityType<?> type = (EntityType<?>)typeObject;
			return "entity/" + Registry.ENTITY_TYPE.getKey(type);
		}

		ClientWorld clientLevel = Minecraft.getInstance().level;
		if (clientLevel == null) { return null; }

		if (uuidObject instanceof UUID)
		{
			UUID uuid = (UUID)uuidObject;

			try
			{
				Object entityStorageObject = Fields.clientLevelEntityStorage.get(clientLevel);
				if(!(entityStorageObject instanceof Int2ObjectMap)) { return null; }
				Int2ObjectMap<Entity> entityStorage = (Int2ObjectMap<Entity>)entityStorageObject;

				for (Entity entity : entityStorage.values())
				{
					if (entity.getUUID().equals(uuid))
					{
						return "entity/" + Registry.ENTITY_TYPE.getKey(entity.getType());
					}
				}
			}
			catch (Exception ignore) {}
		}

		if (playerNameObject instanceof String)
		{
			String playerName = (String)playerNameObject;

			for (PlayerEntity player : clientLevel.players())
			{
				if (player.getGameProfile().getName().equals(playerName))
				{
					return "entity/" + EntityType.getKey(EntityType.PLAYER);
				}
			}
		}

		return null;
	}
}
