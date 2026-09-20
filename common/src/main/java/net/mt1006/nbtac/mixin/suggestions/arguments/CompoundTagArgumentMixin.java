package net.mt1006.nbtac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.mt1006.nbtac.autocomplete.SuggestionManager;
import net.mt1006.nbtac.config.ModConfig;
import net.mt1006.nbtac.utils.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(CompoundTagArgument.class)
public abstract class CompoundTagArgumentMixin implements ArgumentType<CompoundTag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
	{
		try
		{
			String str = builder.getRemaining();
			String name = getResourceName(ctx.getLastChild());
			return SuggestionManager.get(str, name, builder, false);
		}
		catch (Exception e)
		{
			if (ModConfig.debugMode.val) { e.printStackTrace(); }
			return Suggestions.empty();
		}
	}

	@Unique private @Nullable String getResourceName(CommandContext<?> ctx)
	{
		switch (Utils.getCommandName(ctx))
		{
			case "summon":
				EntityType<?> entityType = (EntityType<?>)ctx.getArgument("entity", Holder.Reference.class).value();
				return "entity/" + EntityType.getKey(entityType);

			case "data":
				return getResourceNameForDataCommand(ctx);

			default:
				return null;
		}
	}

	@Unique private @Nullable String getResourceNameForDataCommand(CommandContext<?> ctx)
	{
		String instruction = Utils.getNodeString(ctx, 1);
		if (!instruction.equals("merge")) { return null; }

		return switch (Utils.getNodeString(ctx, 2))
		{
			case "block" -> Utils.blockFromCoords(ctx, "targetPos");
			case "entity" -> Utils.entityFromSelector(ctx, "target");
			default -> null;
		};
	}
}
