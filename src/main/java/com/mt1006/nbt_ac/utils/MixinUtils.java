package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mt1006.nbt_ac.mixin.fields.ClientLevelMixin;
import com.mt1006.nbt_ac.mixin.fields.EntitySelectorMixin;
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
import org.jetbrains.annotations.Nullable;

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
			return "entity/" + RegistryUtils.ENTITY_TYPE.getKey((EntityType<?>)typeTest);
		}

		ClientWorld clientLevel = Minecraft.getInstance().level;
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

		for (PlayerEntity player : clientLevel.players())
		{
			if (player.getGameProfile().getName().equals(playerName))
			{
				return "entity/" + EntityType.getKey(EntityType.PLAYER);
			}
		}
		return null;
	}
}
