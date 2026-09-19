package net.mt1006.nbtac.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.mt1006.nbtac.NBTac;
import net.mt1006.nbtac.mixin.fields.ClientLevelFields;
import net.mt1006.nbtac.mixin.fields.EntitySelectorFields;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	private static final Pattern EXECUTE_START = Pattern.compile("(^|[/ ])(?:minecraft:)?execute ");
	private static final CommandSourceStack DUMMY_COMMAND_SOURCE_STACK =
			new CommandSourceStack(null, Vec3.ZERO, Vec2.ZERO, null, PermissionSet.NO_PERMISSIONS, (MinecraftServer)null, null);

	public static @Nullable String findExecuteAs(String outerCommand)
	{
		// This is far from perfect, but is much simpler then trying to extract
		// "execute as" commands from context. It should work fine in most cases,
		// unless someone is trying to break it on purpose (e.g. using "as" as player name).

		String executeAs = "entity/minecraft:player";

		Matcher executeStartMatcher = EXECUTE_START.matcher(outerCommand);
		if (!executeStartMatcher.find()) { return executeAs; }

		StringReader reader = new StringReader(outerCommand);
		reader.setCursor(executeStartMatcher.end() - 1); // subtract 1 to exclude space

		while (true)
		{
			int asIndex = outerCommand.indexOf(" as ", reader.getCursor());
			if (asIndex == -1) { return executeAs; }

			reader.setCursor(asIndex + 4);
			EntitySelectorParser selectorParser = new EntitySelectorParser(reader, true);

			EntitySelector selector;
			try
			{
				selector = selectorParser.parse();
			}
			catch (Exception ignore)
			{
				// if we fail to parse, maybe it's not selector
				// if it's invalid selector Minecraft will also fail, so suggestion won't be loaded anyway
				reader.setCursor(asIndex + 4); // failing parser might do something stupid with cursor
				continue;
			}

			executeAs = entityFromSelector(selector, executeAs);
		}
	}

	public static String getNodeString(CommandContext<?> ctx, int pos)
	{
		return ctx.getNodes().get(pos).getNode().getName();
	}

	public static String getCommandName(CommandContext<?> ctx)
	{
		String name = getNodeString(ctx, 0);
		return name.startsWith("minecraft:") ? name.substring(10) : name;
	}

	public static Pair<@Nullable String, Integer> getExecuteSubcommandAndOffset(CommandContext<?> ctx)
	{
		if (ctx.getRootNode() instanceof LiteralCommandNode<?> rootNode)
		{
			String rootNodeName = rootNode.getName();
			if (rootNodeName.equals("execute") || rootNodeName.equals("minecraft:execute"))
			{
				return Pair.of(getNodeString(ctx, 0), 0);
			}
		}
		return Pair.of(getCommandName(ctx).equals("execute") ? getNodeString(ctx, 1) : null, 1);
	}

	public static String getArgumentString(CommandContext<?> ctx, String argumentName)
	{
		Map<String, ParsedArgument<?, ?>> arguments;

		try { arguments = (Map<String, ParsedArgument<?, ?>>)Fields.commandContextArguments.get(ctx); }
		catch (Exception e) { return null; }

		ParsedArgument<?, ?> argument = arguments.get(argumentName);

		return argument != null ? argument.getRange().get(ctx.getInput()) : null;
	}

	public static String blockFromCoords(CommandContext<?> ctx, String argName)
	{
		Coordinates coords = ctx.getArgument(argName, Coordinates.class);

		if (!(coords instanceof WorldCoordinates)) { return null; }
		if (coords.isXRelative() || coords.isYRelative() || coords.isZRelative()) { return null; }
		BlockPos blockPos = coords.getBlockPos(DUMMY_COMMAND_SOURCE_STACK);

		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) { return null; }
		Block block = level.getBlockState(blockPos).getBlock();

		return "block/" + RegistryUtils.BLOCK.getKey(block);
	}

	public static @Nullable String entityFromSelector(CommandContext<?> ctx, String argName)
	{
		return entityFromSelector(
				ctx.getArgument(argName, EntitySelector.class),
				findExecuteAs(ctx.getInput().substring(0, ctx.getRange().getStart())));
	}

	private static @Nullable String entityFromSelector(EntitySelector selector, @Nullable String executeAs)
	{
		return entityFromSelectorData(
				((EntitySelectorFields)selector).nbtac$getType(),
				((EntitySelectorFields)selector).nbtac$getEntityUUID(),
				((EntitySelectorFields)selector).nbtac$getPlayerName(),
				selector.isSelfSelector() ? executeAs : null);
	}

	public static @Nullable String entityFromSelectorData(EntityTypeTest<Entity, ?> typeTest, @Nullable UUID uuid,
														  @Nullable String playerName, @Nullable String executeAs)
	{
		if (typeTest instanceof EntityType)
		{
			return "entity/" + RegistryUtils.ENTITY_TYPE.getKey((EntityType<?>)typeTest);
		}

		if (executeAs != null) { return executeAs; }

		ClientLevel clientLevel = Minecraft.getInstance().level;
		if (clientLevel == null) { return null; }

		if (uuid != null)
		{
			try
			{
				TransientEntitySectionManager<Entity> entityStorage = ((ClientLevelFields)clientLevel).nbtac$getEntityStorage();

				Entity entity = entityStorage.getEntityGetter().get(uuid);
				if (entity == null) { return null; }

				return "entity/" + RegistryUtils.ENTITY_TYPE.getKey(entity.getType());
			}
			catch (Exception ignore) {}
		}

		return playerName != null ? "entity/minecraft:player" : null;
	}

	public static boolean isModPresent(String id)
	{
		if (NBTac.loaderInterface == null) { return false; }
		return NBTac.loaderInterface.isModPresent(id);
	}
}
