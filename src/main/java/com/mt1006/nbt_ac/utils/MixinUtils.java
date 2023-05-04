package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mt1006.nbt_ac.mixin.fields.ClientLevelMixin;
import com.mt1006.nbt_ac.mixin.fields.EntitySelectorMixin;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

		return "block/" + RegistryUtils.BLOCK.getKey(block);
	}

	public static String entityFromEntitySelector(EntitySelector entitySelector)
	{
		return entityFromSelectorData(
				((EntitySelectorMixin)entitySelector).getType(),
				((EntitySelectorMixin)entitySelector).getEntityUUID(),
				((EntitySelectorMixin)entitySelector).getPlayerName());
	}

	public static String entityFromSelectorData(EntityType<?> typeTest, @Nullable UUID uuid, @Nullable String playerName)
	{
		if (typeTest != null)
		{
			return "entity/" + RegistryUtils.ENTITY_TYPE.getKey(typeTest);
		}

		ClientLevel clientLevel = Minecraft.getInstance().level;
		if (clientLevel == null) { return null; }

		if (uuid != null)
		{
			try
			{
				Int2ObjectMap<Entity> entityStorage = ((ClientLevelMixin)clientLevel).getEntitiesById();

				for (Entity entity : entityStorage.values())
				{
					if (entity.getUUID().equals(uuid))
					{
						return "entity/" + RegistryUtils.ENTITY_TYPE.getKey(entity.getType());
					}
				}
			}
			catch (Exception ignore) {}
		}

		if (playerName == null) { return null; }

		for (Player player : clientLevel.players())
		{
			if (player.getGameProfile().getName().equals(playerName))
			{
				return "entity/" + EntityType.getKey(EntityType.PLAYER);
			}
		}
		return null;
	}
}
