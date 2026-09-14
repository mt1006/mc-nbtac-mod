package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.types.templates.Tag;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.autocomplete.type.compound.CompoundType;
import net.mt1006.nbtac.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(NbtTagArgument.class)
public abstract class NbtTagArgumentMixin implements ArgumentType<Tag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		try
		{
			String str = builder.getRemaining();
			Type tagType = getTagType(ctx, "entity/minecraft:player");
			return tagType != null ? SuggestionManager.get(str, tagType, builder, false, Function.identity()) : Suggestions.empty();
		}
		catch (Exception e)
		{
			return Suggestions.empty();
		}
	}

	@Unique private @Nullable Type getTagType(CommandContext<?> ctx, String executeAs)
	{
		String commandName = Utils.getCommandName(ctx);
		if (commandName.equals("data"))
		{
			return getTagTypeForDataCommand(ctx, executeAs);
		}
		else if (ctx.getChild() != null)
		{
			if (Objects.equals(Utils.getExecuteSubcommand(ctx), "as"))
			{
				executeAs = Utils.entityFromEntitySelector(ctx, "targets", executeAs);
			}

			return getTagType(ctx.getChild(), executeAs);
		}
		return null;
	}

	@Unique private @Nullable Type getTagTypeForDataCommand(CommandContext<?> ctx, String executeAs)
	{
		String instruction = Utils.getNodeString(ctx, 1);
		if (!instruction.equals("modify")) { return null; }

		String type = Utils.getNodeString(ctx, 2);
		String path = Utils.getArgumentString(ctx, "targetPath");

		String root;
		switch (type)
		{
			case "block":
				root = Utils.blockFromCoords(ctx, "targetPos");
				break;

			case "entity":
				root = Utils.entityFromEntitySelector(ctx, "target", executeAs);
				break;

			default:
				return null;
		}
		if (root == null) { return null; }

		CustomTagParser parser = CustomTagParser.forNbtPath(path, CompoundType.fromName(root));
		parser.parse();
		return parser.pathType;
	}
}
