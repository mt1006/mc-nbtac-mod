package com.mt1006.nbt_ac.mixin.suggestions.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.Utils;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(CompoundTagArgument.class)
public abstract class CompoundTagArgumentMixin implements ArgumentType<CompoundTag>
{
	@Override public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder)
	{
		try
		{
			String name = getResourceName(commandContext);
			if (name == null) { return Suggestions.empty(); }

			String tag = suggestionsBuilder.getRemaining();

			return NbtSuggestionManager.loadFromName(name, tag, suggestionsBuilder, false);
		}
		catch (Exception exception)
		{
			return Suggestions.empty();
		}
	}

	@Unique private @Nullable String getResourceName(CommandContext<?> ctx)
	{
		String commandName = Utils.getCommandName(ctx);

		switch (commandName)
		{
			case "summon":
				EntityType<?> entityType = (EntityType<?>)ctx.getArgument("entity", Holder.Reference.class).value();
				ResourceLocation resourceLocation = EntityType.getKey(entityType);
				return "entity/" + resourceLocation;

			case "data":
				return getResourceNameForDataCommand(ctx);

			default:
				if (ctx.getChild() != null) { return getResourceName(ctx.getChild()); }
		}
		return null;
	}

	@Unique private @Nullable String getResourceNameForDataCommand(CommandContext<?> ctx)
	{
		String instruction = Utils.getNodeString(ctx, 1);
		if (!instruction.equals("merge")) { return null; }
		String targetType = Utils.getNodeString(ctx, 2);

		switch (targetType)
		{
			case "block":
				Coordinates coords = ctx.getArgument("targetPos", Coordinates.class);
				return Utils.blockFromCoords(coords);

			case "entity":
				EntitySelector entitySelector = ctx.getArgument("target", EntitySelector.class);
				return Utils.entityFromEntitySelector(entitySelector);
		}
		return null;
	}
}
