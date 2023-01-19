package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class MixinUtils
{
	private static final CommandSourceStack DUMMY_COMMAND_SOURCE_STACK =
			new CommandSourceStack(null, Vec3.ZERO, Vec2.ZERO, null, 0, null, null, null, null);

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

	public static String blockFromCoords(Coordinates coords)
	{
		if (!(coords instanceof WorldCoordinates)) { return null; }
		if (coords.isXRelative() || coords.isYRelative() || coords.isZRelative()) { return null; }
		BlockPos blockPos = coords.getBlockPos(DUMMY_COMMAND_SOURCE_STACK);

		ClientLevel level = Minecraft.getInstance().level;
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

		ClientLevel clientLevel = Minecraft.getInstance().level;
		if (clientLevel == null) { return null; }

		if (uuidObject instanceof UUID)
		{
			UUID uuid = (UUID)uuidObject;

			try
			{
				Object entityStorageObject = Fields.clientLevelEntityStorage.get(clientLevel);
				if(!(entityStorageObject instanceof TransientEntitySectionManager)) { return null; }
				TransientEntitySectionManager<?> entityStorage = (TransientEntitySectionManager<?>)entityStorageObject;

				EntityAccess entityAccess = entityStorage.getEntityGetter().get(uuid);
				if (!(entityAccess instanceof Entity)) { return null; }
				Entity entity = (Entity)entityAccess;

				return "entity/" + Registry.ENTITY_TYPE.getKey(entity.getType());
			}
			catch (Exception ignore) {}
		}

		if (playerNameObject instanceof String)
		{
			String playerName = (String)playerNameObject;

			for (Player player : clientLevel.players())
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
