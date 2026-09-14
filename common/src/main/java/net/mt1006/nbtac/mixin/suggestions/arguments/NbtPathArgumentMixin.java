package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mixin(NbtPathArgument.class)
public abstract class NbtPathArgumentMixin implements ArgumentType<CompoundTag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		try
		{
			String str = builder.getRemaining();
			String name = getResourceName(ctx, builder.getStart(), "entity/minecraft:player");
			return SuggestionManager.get(str, name, builder, true);
		}
		catch (Exception e)
		{
			if (ModConfig.debugMode.val) { e.printStackTrace(); }
			return Suggestions.empty();
		}
	}

	@Unique private @Nullable String getResourceName(CommandContext<?> ctx, int cursor, String executeAs)
	{
		String commandName = Utils.getCommandName(ctx);
		Pair<@Nullable String, Integer> executeSubcommandPair = Utils.getExecuteSubcommandAndOffset(ctx);
		String executeSubcommand = executeSubcommandPair.getFirst();
		int executeSubcommandOffset = executeSubcommandPair.getSecond();

		if (ctx.getRange().getEnd() < cursor && ctx.getChild() != null)
		{
			if (Objects.equals(executeSubcommand, "as"))
			{
				executeAs = Utils.entityFromEntitySelector(ctx, "targets", executeAs);
			}
			return getResourceName(ctx.getChild(), cursor, executeAs);
		}

		if (ctx.getRootNode() instanceof RootCommandNode<?> && commandName.equals("data"))
		{
			return getResourceNameForDataCommand(ctx, executeAs);
		}
		else if (executeSubcommand != null)
		{
			switch (executeSubcommand)
			{
				case "if", "unless":
					return getResourceNameForExecuteCommand(ctx, true, executeSubcommandOffset, executeAs);

				case "store":
					return getResourceNameForExecuteCommand(ctx, false, executeSubcommandOffset, executeAs);
			}
		}
		return null;
	}

	@Unique private @Nullable String getResourceNameForDataCommand(CommandContext<?> ctx, String executeAs)
	{
		String blockArgument = "targetPos";
		String entityArgument = "target";
		String instruction = Utils.getNodeString(ctx, 1);
		String type = Utils.getNodeString(ctx, 2);

		switch (instruction)
		{
			case "get":
			case "remove":
				break;

			case "modify":
				if (ctx.getNodes().size() > 7)
				{
					String modification = Utils.getNodeString(ctx, 5);

					if (modification.equals("insert")) { type = Utils.getNodeString(ctx, 8); }
					else { type = Utils.getNodeString(ctx, 7); }

					blockArgument = "sourcePos";
					entityArgument = "source";
				}
				break;

			default:
				return null;
		}

		return getResourceNameForArguments(ctx, type, blockArgument, entityArgument, executeAs);
	}

	@Unique private @Nullable String getResourceNameForExecuteCommand(CommandContext<?> ctx, boolean isIf, int offset, String executeAs)
	{
		if (isIf && !Utils.getNodeString(ctx, 1 + offset).equals("data")) { return null; }

		String type = Utils.getNodeString(ctx, 2 + offset);
		return getResourceNameForArguments(ctx, type, isIf ? "sourcePos" : "targetPos", isIf ? "source" : "target", executeAs);
	}

	@Unique private @Nullable String getResourceNameForArguments(CommandContext<?> ctx, String type, String blockArgument,
																 String argument, String executeAs)
	{
		switch (type)
		{
			case "block":
				return Utils.blockFromCoords(ctx, blockArgument);

			case "entity":
				return Utils.entityFromEntitySelector(ctx, argument, executeAs);

			case "storage":
				Identifier id = ctx.getArgument(argument, Identifier.class);
				return "storage/" + id;

			default:
				return null;
		}
	}
}
