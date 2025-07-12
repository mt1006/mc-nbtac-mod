package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.types.templates.Tag;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.mt1006.nbtac.autocomplete.NbtTagManager;
import net.mt1006.nbtac.autocomplete.NbtTagMap;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.autocomplete.parser.CustomTagParser;
import net.mt1006.nbtac.autocomplete.type.Type;
import net.mt1006.nbtac.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(NbtTagArgument.class)
public abstract class NbtTagArgumentMixin implements ArgumentType<Tag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		try
		{
			String str = builder.getRemaining();
			Type tagType = getTagType(ctx);
			return tagType != null ? SuggestionManager.loadFromType(str, tagType, builder) : Suggestions.empty();
		}
		catch (Exception e)
		{
			return Suggestions.empty();
		}
	}

	@Unique private @Nullable Type getTagType(CommandContext<?> ctx)
	{
		String commandName = Utils.getCommandName(ctx);
		if (commandName.equals("data")) { return getTagTypeForDataCommand(ctx); }
		else if (ctx.getChild() != null) { return getTagType(ctx.getChild()); }
		return null;
	}

	@Unique private @Nullable Type getTagTypeForDataCommand(CommandContext<?> ctx)
	{
		String instruction = Utils.getNodeString(ctx, 1);
		if (!instruction.equals("modify")) { return null; }

		String type = Utils.getNodeString(ctx, 2);
		String path = Utils.getArgumentString(ctx, "targetPath");

		String root;
		switch (type)
		{
			case "block":
				Coordinates coords = ctx.getArgument("targetPos", Coordinates.class);
				root = Utils.blockFromCoords(coords);
				break;

			case "entity":
				EntitySelector entitySelector = ctx.getArgument("target", EntitySelector.class);
				root = Utils.entityFromEntitySelector(entitySelector);
				break;

			default:
				return null;
		}
		if (root == null) { return null; }

		NbtTagMap rootTagMap = NbtTagManager.get(root);
		if (rootTagMap == null) { return null; }

		CustomTagParser parser = CustomTagParser.forNbtPath(path, rootTagMap);
		parser.parse();
		return parser.parsedPathType;
	}
}
