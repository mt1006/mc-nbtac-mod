package com.mt1006.nbt_ac.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mt1006.nbt_ac.config.ModConfig;
import com.mt1006.nbt_ac.mixin.fields.ClientLevelFields;
import com.mt1006.nbt_ac.mixin.fields.EntitySelectorFields;
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class Utils
{
	private static final CommandSourceStack DUMMY_COMMAND_SOURCE_STACK =
			new CommandSourceStack(null, Vec3.ZERO, Vec2.ZERO, null, 0, null, null, null, null);

	public static String getNodeString(CommandContext<?> ctx, int pos)
	{
		return ctx.getNodes().get(pos).getNode().getName();
	}

	public static String getCommandName(CommandContext<?> ctx)
	{
		String name = getNodeString(ctx, 0);
		if (ModConfig.supportCommandNamespace.val && name.startsWith("minecraft:"))
		{
			return name.substring(10);
		}
		return name;
	}

	public static String getArgumentString(CommandContext<?> ctx, String argumentName)
	{
		Map<String, ParsedArgument<?, ?>> arguments;

		try { arguments = (Map<String, ParsedArgument<?, ?>>)Fields.commandContextArguments.get(ctx); }
		catch (Exception exception) { return null; }

		ParsedArgument<?, ?> argument = arguments.get(argumentName);
		if (argument == null) { return null; }

		return argument.getRange().get(ctx.getInput());
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
				((EntitySelectorFields)entitySelector).getType(),
				((EntitySelectorFields)entitySelector).getEntityUUID(),
				((EntitySelectorFields)entitySelector).getPlayerName());
	}

	public static String entityFromSelectorData(EntityTypeTest<Entity, ?> typeTest, @Nullable UUID uuid, @Nullable String playerName)
	{
		if (typeTest instanceof EntityType)
		{
			return "entity/" + RegistryUtils.ENTITY_TYPE.getKey((EntityType<?>)typeTest);
		}

		ClientLevel clientLevel = Minecraft.getInstance().level;
		if (clientLevel == null) { return null; }

		if (uuid != null)
		{
			try
			{
				TransientEntitySectionManager<Entity> entityStorage = ((ClientLevelFields)clientLevel).getEntityStorage();

				Entity entity = entityStorage.getEntityGetter().get(uuid);
				if (entity == null) { return null; }

				return "entity/" + RegistryUtils.ENTITY_TYPE.getKey(entity.getType());
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

	public static boolean isModPresent(String id)
	{
		return FabricLoader.getInstance().getModContainer(id).isPresent();
	}
}
