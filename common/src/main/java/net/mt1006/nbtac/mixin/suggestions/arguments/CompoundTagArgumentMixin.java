package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mixin(CompoundTagArgument.class)
public abstract class CompoundTagArgumentMixin implements ArgumentType<CompoundTag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		try
		{
			String str = builder.getRemaining();
			String name = getResourceName(ctx, "entity/minecraft:player");
			return SuggestionManager.get(str, name, builder, false);
		}
		catch (Exception e)
		{
			if (ModConfig.debugMode.val) { e.printStackTrace(); }
			return Suggestions.empty();
		}
	}

	@Unique private @Nullable String getResourceName(CommandContext<?> ctx, String executeAs)
	{
		String commandName = Utils.getCommandName(ctx);

		switch (commandName)
		{
			case "summon":
				EntityType<?> entityType = (EntityType<?>)ctx.getArgument("entity", Holder.Reference.class).value();
				Identifier id = EntityType.getKey(entityType);
				return "entity/" + id;

			case "data":
				return getResourceNameForDataCommand(ctx, executeAs);

			default:
				if (Objects.equals(Utils.getExecuteSubcommand(ctx), "as"))
				{
					executeAs = Utils.entityFromEntitySelector(ctx, "targets", executeAs);
				}

				return ctx.getChild() != null ? getResourceName(ctx.getChild(), executeAs) : null;
		}
	}

	@Unique private @Nullable String getResourceNameForDataCommand(CommandContext<?> ctx, @Nullable String executeAs)
	{
		String instruction = Utils.getNodeString(ctx, 1);
		if (!instruction.equals("merge")) { return null; }
		String targetType = Utils.getNodeString(ctx, 2);

		switch (targetType)
		{
			case "block":
				return Utils.blockFromCoords(ctx, "targetPos");

			case "entity":
				return Utils.entityFromEntitySelector(ctx, "target", executeAs);

			default:
				return null;
		}
	}
}
