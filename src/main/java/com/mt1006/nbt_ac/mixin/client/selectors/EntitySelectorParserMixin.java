package com.mt1006.nbt_ac.mixin.client.selectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestionManager;
import com.mt1006.nbt_ac.utils.MixinUtils;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorParserMixin
{
	@Shadow @Final private StringReader reader;
	@Shadow private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions;
	@Shadow @Nullable private EntityType<?> type;
	@Shadow @Nullable private UUID entityUUID;
	@Shadow @Nullable private String playerName;
	private String lastTag = null;

	@ModifyVariable(method = "parseOptions", at = @At("STORE"), ordinal = 0)
	public String parseOptionsModifyString(String str)
	{
		lastTag = str;
		return str;
	}

	@Redirect(method = "parseOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/options/EntitySelectorOptions$Modifier;handle(Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;)V"))
	private void atParseOptions(EntitySelectorOptions.Modifier modifier, EntitySelectorParser parser) throws CommandSyntaxException
	{
		if (lastTag != null && lastTag.equalsIgnoreCase("nbt"))
		{
			int cursor = reader.getCursor();

			try
			{
				modifier.handle(parser);
			}
			catch (CommandSyntaxException exception)
			{
				reader.setCursor(cursor);
				suggestions = this::suggestNbt;
				throw exception;
			}
		}
		else
		{
			modifier.handle(parser);
		}
	}

	private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer)
	{
		String name = MixinUtils.entityFromSelectorData(type, entityUUID, playerName);
		String tag = suggestionsBuilder.getRemaining();

		return NbtSuggestionManager.loadSuggestions(NbtSuggestionManager.get(name),
				"$tag/" + name, tag, suggestionsBuilder, false).buildFuture();
	}
}
